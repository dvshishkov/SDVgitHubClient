package com.shishkov.android.sdvgithubclient.authorization.controller.Activity;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shishkov.android.sdvgithubclient.R;
import com.shishkov.android.sdvgithubclient.authorization.model.GitHubAuthorizationModel;
import com.shishkov.android.sdvgithubclient.common.BaseSwipeToRefreshActivity;
import com.shishkov.android.sdvgithubclient.common.Model;
import com.shishkov.android.sdvgithubclient.common.ModelRetainFragment;
import com.shishkov.android.sdvgithubclient.utils.AccountUtils;
import com.shishkov.android.sdvgithubclient.utils.ConnectionCheck;
import com.shishkov.android.sdvgithubclient.utils.ViewUtils;
import com.shishkov.android.sdvgithubclient.workFlow.controller.activity.RepositoriesActivity;

import org.eclipse.egit.github.core.User;

public class AuthorizationActivity extends BaseSwipeToRefreshActivity {

	private static final String TAG = "AuthorizationActivity";
	public static final String USER_EXTRA = "USER_EXTRA";

	private AccountManager mAccountManager;
	private Account account;
	private FrameLayout flZeroData;
	private TextView tvZeroDataText;
	private ProgressDialog pdProgressDialog;
	private GitHubAuthorizationModel gitHubAuthorizationModel;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authorization);

		flZeroData = (FrameLayout) findViewById(R.id.zero_data);
		flZeroData.setVisibility(View.GONE);
		tvZeroDataText = (TextView) findViewById(R.id.zero_data_text);

		mAccountManager = AccountManager.get(this);
		account = AccountUtils.getAccount(mAccountManager);

		//if no account (first installed/reinstalled or logged out = deleted)
		//else we have account on device with login, password and auth token stored in it
		if (account == null) {
			LoginActivity.startForResult(this);
		} else {
			authorize();
		}
	}

	@Override
	protected void handleOnRefresh() {
		authorize();
	}

	private void authorize() {
		if (!ConnectionCheck.isOnline(this)) {
			if (mRefreshing) {
				Toast.makeText(this, R.string.offline_still_no_internet, Toast.LENGTH_SHORT).show();
			} else {
				showZeroData(getString(R.string.offline_pull));
			}
			setRefreshing(false);
		} else {
			flZeroData.setVisibility(View.GONE);
			ModelRetainFragment retainFragment = ModelRetainFragment.getRetainFragment(this);
			gitHubAuthorizationModel = (GitHubAuthorizationModel) retainFragment.getModel(new Model.Creator() {
				@Override
				public Model createNewModel() {
					return new GitHubAuthorizationModel(AuthorizationActivity.this);
				}
			});
			//check for handling rotation
			if (gitHubAuthorizationModel.isLoggingIn()) {
				loginUsingDataFromAccount();
			} else {
				checkAuthorization();
			}
		}
	}

	/**
	 * Method checks if the user is currently authorized.
	 * If he is - opens workFlow.
	 */
	private void checkAuthorization() {
		pdProgressDialog = ViewUtils.showProgressDialog(this, getString(R.string.signing_in_please_wait));
		gitHubAuthorizationModel.setRequestUserListener(new GitHubAuthorizationModel.RequestUserListener() {
			@Override
			public void onRequestSuccess(User user) {
				pdProgressDialog.dismiss();
				RepositoriesActivity.start(AuthorizationActivity.this, user);
				finish();
			}

			@Override
			public void onRequestError() {
				loginUsingDataFromAccount();
			}
		});
		gitHubAuthorizationModel.checkAuthorization(account);
	}

	/**
	 * Method tries to login using data stored in account on device
	 */
	private void loginUsingDataFromAccount() {
		gitHubAuthorizationModel.setLoginListener(new GitHubAuthorizationModel.LoginListener() {
			@Override
			public void needTwoFactorAuth() {
				pdProgressDialog.dismiss();
				TwoFactorAuthActivity.startForResult(AuthorizationActivity.this, account.name, mAccountManager.getPassword(account));
			}

			@Override
			public void onLoginSuccess(User user) {
				pdProgressDialog.dismiss();
				RepositoriesActivity.start(AuthorizationActivity.this, user);
				finish();
			}

			@Override
			public void onLoginError() {
				pdProgressDialog.dismiss();
				LoginActivity.startForResult(AuthorizationActivity.this);
			}

			@Override
			public void onInvalidLoginPassword() {
				pdProgressDialog.dismiss();
				Toast.makeText(AuthorizationActivity.this, R.string.wrong_login_or_pass, Toast.LENGTH_SHORT).show();
				LoginActivity.startForResult(AuthorizationActivity.this);
			}

			@Override
			public void onTokenAlreadyExists() {
				//cannot happen
			}

			@Override
			public void onWrongOtpCode() {
				//cannot happen
			}
		});
		gitHubAuthorizationModel.login(account.name, mAccountManager.getPassword(account));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
			case RESULT_OK:
				User user = (User) data.getSerializableExtra(AuthorizationActivity.USER_EXTRA);
				RepositoriesActivity.start(AuthorizationActivity.this, user);
				finish();
				break;
			case RESULT_CANCELED:
				finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (gitHubAuthorizationModel != null) {
			gitHubAuthorizationModel.clearRequestUserListener();
			gitHubAuthorizationModel.clearLoginListener();

			if (isFinishing()) {
				gitHubAuthorizationModel.stopRequestUser();
				gitHubAuthorizationModel.stopLoggingIn();
			}
		}
		if (pdProgressDialog != null) {
			pdProgressDialog.dismiss();
			pdProgressDialog = null;
		}
	}

	private void showZeroData(String message) {
		flZeroData.setVisibility(View.VISIBLE);
		tvZeroDataText.setText(message);
	}

	public static void start(Activity activity) {
		Intent intent = new Intent(activity, AuthorizationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
		activity.startActivity(intent);
	}
}
