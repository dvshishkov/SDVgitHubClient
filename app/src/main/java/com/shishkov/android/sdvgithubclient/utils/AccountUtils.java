package com.shishkov.android.sdvgithubclient.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.shishkov.android.sdvgithubclient.utils.callbacks.TaskCallback;

import org.eclipse.egit.github.core.User;

import java.io.IOException;

public class AccountUtils {

	private static final String ACCOUNT_TYPE = "com.shishkov.android.sdvgithubclient";

	public static Account getAccount(AccountManager accountManager) {
		final Account[] accounts = accountManager
				.getAccountsByType(ACCOUNT_TYPE);
		return accounts.length > 0 ? accounts[0] : null;
	}

	public static void getAuthTokenAsync(Activity activity, Account account, AccountManager accountManager, final TaskCallback<String> callback) {
		Handler handler = new Handler();
		accountManager.getAuthToken(account, ACCOUNT_TYPE, null, activity, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				try {
					Bundle result = future.getResult();
					String s = result != null ? result.getString(AccountManager.KEY_AUTHTOKEN) : null;
					callback.onSuccess(s);
				} catch (AccountsException | IOException e) {
					e.printStackTrace();
					callback.onError();
				}
			}
		}, handler);
	}

	public static String getAuthToken(Activity activity, Account account, AccountManager accountManager) {
		AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account, ACCOUNT_TYPE, null, activity, null, null);
		try {
			Bundle result = future.getResult();
			return result != null ? result.getString(AccountManager.KEY_AUTHTOKEN) : null;
		} catch (AccountsException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void createOrUpdateAccount(AccountManager accountManager, Account account, User user, String password, String authToken) {
		if (account == null) {
			account = new Account(user.getLogin(), ACCOUNT_TYPE);
			accountManager.addAccountExplicitly(account, password, null);
		}
		accountManager.setAuthToken(account, ACCOUNT_TYPE, authToken);
	}

	public static void removeAccount(AccountManager accountManager, Account account, final TaskCallback<Void> callback) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			accountManager.removeAccount(account, null, new AccountManagerCallback<Bundle>() {
				@Override
				public void run(AccountManagerFuture<Bundle> future) {
					try {
						boolean wasAccountDeleted = future.getResult().getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
						if (wasAccountDeleted) {
							callback.onSuccess(null);
							return;
						}
					} catch (OperationCanceledException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (AuthenticatorException e) {
						e.printStackTrace();
					}
					callback.onError();
				}
			}, null);

		} else {
			accountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
				@Override
				public void run(AccountManagerFuture<Boolean> future) {
					try {
						boolean wasAccountDeleted = future.getResult();
						if (wasAccountDeleted) {
							callback.onSuccess(null);
							return;
						}
					} catch (OperationCanceledException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (AuthenticatorException e) {
						e.printStackTrace();
					}
					callback.onError();
				}
			}, null);
		}
	}
}
