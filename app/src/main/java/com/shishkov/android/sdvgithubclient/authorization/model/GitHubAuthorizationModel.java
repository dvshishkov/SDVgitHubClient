package com.shishkov.android.sdvgithubclient.authorization.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.AsyncTask;

import com.shishkov.android.sdvgithubclient.authorization.model.exceptions.TwoFactorAuthException;
import com.shishkov.android.sdvgithubclient.authorization.model.objects.SupportedAuthorization;
import com.shishkov.android.sdvgithubclient.common.Model;
import com.shishkov.android.sdvgithubclient.utils.AccountUtils;
import com.shishkov.android.sdvgithubclient.utils.callbacks.TaskCallback;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;

public class GitHubAuthorizationModel implements Model {

	private Activity mActivity;
	private AccountManager mAccountManager;
	private LoginTask mLoginTask;
	private AsyncTask mRequestUserTask;
	private LoginListener mLoginListener;
	private RequestUserListener mRequestUserListener;
	private boolean mLoggingIn;
	private boolean mRequestingUser;
	private Error mError;

	public GitHubAuthorizationModel(Activity activity) {
		this.mActivity = activity;
		mAccountManager = AccountManager.get(activity);
	}

	public void login(String username, String password) {
		if (mLoggingIn) {
			return;
		}
		mLoggingIn = true;
		mLoginTask = new LoginTask(username, password);
		mLoginTask.execute();
	}

	public void loginTwoFactor(String username, String password, String otpCode) {
		if (mLoggingIn) {
			return;
		}
		mLoggingIn = true;
		mLoginTask = new LoginTask(username, password, otpCode);
		mLoginTask.execute();
	}

	public void setLoginListener(LoginListener loginListener) {
		mLoginListener = loginListener;
	}

	public boolean isLoggingIn() {
		return mLoggingIn;
	}

	public void stopLoggingIn() {
		if (mLoggingIn) {
			if (mLoginTask != null) {
				mLoginTask.cancel(true);
			}
			mLoggingIn = false;
		}
	}

	public void clearLoginListener() {
		mLoginListener = null;
	}

	private class LoginTask extends AsyncTask<Void, Void, User> {
		private String mUsername;
		private String mPassword;
		private String mOtpCode;

		public LoginTask(String username, String password) {
			this.mUsername = username;
			this.mPassword = password;
		}

		public LoginTask(String username, String password, String otpCode) {
			this(username, password);
			this.mOtpCode = otpCode;
		}

		@Override
		protected User doInBackground(final Void... params) {
			mError = null;

			//we set two-factor client to be able to check if user have two-factor authorization
			TwoFactorAuthClient client = initTwoFactorAuthClient();

			//first enter always here
			//this block is for check if user has two-factor authorization or entered wrong login / pass
			//we won`t return user here, because we don`t want to authorize using credentials each time
			//(that won`t be Oauth authorization, which we needed)
			//Oauth authorization logic goes right after this block
			if (mOtpCode == null) {
				try {
					new UserService(client).getUser();
				} catch (TwoFactorAuthException e) {
					mError = Error.NEED_TWO_FACTOR_AUTH;
					if (e.twoFactorAuthType == TwoFactorAuthClient.TWO_FACTOR_AUTH_TYPE_SMS) {
						sendSmsOtpCode(new OAuthService(client));
					}
					return null;
				} catch (IOException e) {
					mError = Error.WRONG_LOGIN_OR_PASSWORD;
					return null;
				}
			}

			Account account = AccountUtils.getAccount(mAccountManager);
			String authToken = null;

			//authorization here means personal access tokens on GitHub (which function like ordinary OAuth access tokens)
			//see https://github.com/settings/tokens
			SupportedAuthorization authorization;
			SupportedOAuthService service = new SupportedOAuthService(client);

			try {
				try {
					//if we pass here without error
					//the only reason we can fall with IOException here is wrong otp code in client
					authorization = GitHubApiClient.getAuthorization(service);
				} catch (IOException e) {
					mError = Error.WRONG_OTP_CODE;
					return null;
				}
				if (authorization == null) {
					authToken = GitHubApiClient.createAuthorization(service);

				} else {
					if (account != null) {
						//we have an account (with credentials stored) and we have authorization with token on GitHub
						//so we need to compare tokens to confirm token stored in account on device

						String authTokenInAccount = AccountUtils.getAuthToken(mActivity, account, mAccountManager);
						String authTokenInAccountLastEight = authTokenInAccount.substring(authTokenInAccount.length() - 8);

						if (authorization.getTokenLastEight().equals(authTokenInAccountLastEight)) {
							authToken = authTokenInAccount;
						} else {
							// If auth token in account not equals to auth token on GitHub
							// we cannot request GitHub auth token due to method deprecation
							// (now we can only get token last eight symbols) so we have to delete it and create new one
							// See: https://developer.github.com/v3/oauth_authorizations/#reset-an-authorization
							try {
								service.deleteAuthorization(authorization.getId());
							} catch (IOException e) {
								//we cannot delete authorization from app, if user have two-factor authorization
								//GitHub just does not allow it
								//so an attempt to delete it will throw IO Exception
								mError = Error.TOKEN_ALREADY_EXISTS;
								return null;
							}
							GitHubApiClient.getAuthorization(service); // need to request authorizations after deletion before creating new one
							authToken = GitHubApiClient.createAuthorization(service);
						}
					} else {
						try {
							service.deleteAuthorization(authorization.getId());
						} catch (IOException e) {
							mError = Error.TOKEN_ALREADY_EXISTS;
							return null;
						}
						GitHubApiClient.getAuthorization(service); // need to request authorizations after deletion before creating new one
						authToken = GitHubApiClient.createAuthorization(service);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (authToken == null || authToken.isEmpty()) {
				mError = Error.UNKNOWN_LOGIN_ERROR;
				return null;
			}

			try {
				client.setOAuth2Token(authToken);
				User user = new UserService(client).getUser();
				AccountUtils.createOrUpdateAccount(mAccountManager, account, user, mPassword, authToken);
				return user;
			} catch (IOException e) {
				mError = Error.UNKNOWN_LOGIN_ERROR;
				return null;
			}
		}

		@Override
		protected void onPostExecute(User user) {
			mLoggingIn = false;
			if (user != null) {
				mLoginListener.onLoginSuccess(user);
			} else {
				switch (mError) {
					case NEED_TWO_FACTOR_AUTH:
						mLoginListener.needTwoFactorAuth();
						break;
					case WRONG_LOGIN_OR_PASSWORD:
						mLoginListener.onInvalidLoginPassword();
						break;
					case WRONG_OTP_CODE:
						mLoginListener.onWrongOtpCode();
						break;
					case TOKEN_ALREADY_EXISTS:
						mLoginListener.onTokenAlreadyExists();
						break;
					case UNKNOWN_LOGIN_ERROR:
						mLoginListener.onLoginError();
						break;
				}
			}
		}

		private TwoFactorAuthClient initTwoFactorAuthClient() {
			TwoFactorAuthClient client = new TwoFactorAuthClient();
			client.setCredentials(mUsername, mPassword);
			if (mOtpCode != null) {
				client.setOtpCode(mOtpCode);
			}
			return client;
		}
	}

	public static void sendSmsOtpCode(final OAuthService service) {
		try {
			GitHubApiClient.createAuthorization(service);
		} catch (IOException ignored) {
		}
	}


	/**
	 * This method is used every time user opens application after his first sign in.
	 * Uses Oauth token stored in account on the device to automatically open users GitHub profile.
	 */
	public void checkAuthorization(Account account) {
		if (mRequestingUser) {
			return;
		}
		mRequestingUser = true;
		AccountUtils.getAuthTokenAsync(mActivity, account, mAccountManager, new TaskCallback<String>() {
			@Override
			public void onSuccess(final String authToken) {

				mRequestUserTask = new AsyncTask<Void, Void, User>() {
					@Override
					protected User doInBackground(Void... params) {
						try {
							GitHubClient client = new GitHubClient();
							client.setOAuth2Token(authToken);
							return new UserService(client).getUser();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void onPostExecute(User user) {
						mRequestingUser = false;
						if (user == null) {
							mRequestUserListener.onRequestError();
						} else {
							mRequestUserListener.onRequestSuccess(user);
						}
					}
				}.execute();
			}

			@Override
			public void onError() {
				mRequestUserListener.onRequestError();
			}
		});
	}

	public void setRequestUserListener(RequestUserListener requestUserListener) {
		mRequestUserListener = requestUserListener;
	}

	public void stopRequestUser() {
		if (mRequestingUser) {
			if (mRequestUserTask != null) {
				mRequestUserTask.cancel(true);
			}
			mRequestingUser = false;
		}
	}

	public void clearRequestUserListener() {
		mRequestUserListener = null;
	}


	public interface LoginListener {
		void needTwoFactorAuth();

		void onLoginSuccess(User user);

		void onLoginError();

		void onInvalidLoginPassword();

		void onTokenAlreadyExists();

		void onWrongOtpCode();
	}

	public interface RequestUserListener {
		void onRequestSuccess(User user);

		void onRequestError();
	}

	private enum Error {
		NEED_TWO_FACTOR_AUTH,
		WRONG_LOGIN_OR_PASSWORD,
		WRONG_OTP_CODE,
		TOKEN_ALREADY_EXISTS,
		UNKNOWN_LOGIN_ERROR
	}
}
