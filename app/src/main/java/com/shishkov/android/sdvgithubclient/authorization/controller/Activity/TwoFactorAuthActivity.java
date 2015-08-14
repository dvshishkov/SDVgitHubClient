package com.shishkov.android.sdvgithubclient.authorization.controller.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shishkov.android.sdvgithubclient.R;
import com.shishkov.android.sdvgithubclient.authorization.controller.Fragment.DeleteAuthorizationWarningDialog;
import com.shishkov.android.sdvgithubclient.authorization.model.GitHubAuthorizationModel;
import com.shishkov.android.sdvgithubclient.common.BaseToolbarActivity;
import com.shishkov.android.sdvgithubclient.common.Model;
import com.shishkov.android.sdvgithubclient.common.ModelRetainFragment;
import com.shishkov.android.sdvgithubclient.utils.ConnectionCheck;
import com.shishkov.android.sdvgithubclient.utils.ViewUtils;

import org.eclipse.egit.github.core.User;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class TwoFactorAuthActivity extends BaseToolbarActivity implements DeleteAuthorizationWarningDialog.YesToFinishListener {


	public static final int GIT_HUB_OTP_CODE_LENGTH = 6;
	public static final String OTP_CODE_RECEIVED = "com.shishkov.android.sdvgithubclient.OTP_CODE_RECEIVED";
	public static final String EXTRA_OTP_CODE = "EXTRA_OTP_CODE";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_PASSWORD = "password";

	private EditText etOtpCodeText;
	private ProgressDialog pdProgressDialog;
	private MenuItem mLoginItem;
	private String mUsername;
	private String mPassword;
	private BroadcastReceiver mOtpCodeReceiver;
	private GitHubAuthorizationModel mGitHubAuthorizationModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two_factor_auth);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		etOtpCodeText = (EditText) findViewById(R.id.et_otp_code);

		Intent intent = getIntent();
		mUsername = intent.getStringExtra(PARAM_USERNAME);
		mPassword = intent.getStringExtra(PARAM_PASSWORD);
		setWatcherForOtp();

		//hardware keyboard done button to log in
		etOtpCodeText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event != null && ACTION_DOWN == event.getAction()
						&& keyCode == KEYCODE_ENTER && loginEnabled()) {
					handleLogin();
					return true;
				} else {
					return false;
				}
			}
		});

		//software keyboard done button to log in
		etOtpCodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == IME_ACTION_DONE && loginEnabled()) {
					handleLogin();
					return true;
				} else {
					return false;
				}
			}
		});
	}

	private void handleLogin() {
		if (!ConnectionCheck.isOnline(this)) {
			Toast.makeText(TwoFactorAuthActivity.this, R.string.offline, Toast.LENGTH_SHORT).show();
		} else {
			final String otpCode = etOtpCodeText.getText().toString();
			pdProgressDialog = ViewUtils.showProgressDialog(this, getString(R.string.signing_in_please_wait));

			ModelRetainFragment retainFragment = ModelRetainFragment.getRetainFragment(this);
			mGitHubAuthorizationModel = (GitHubAuthorizationModel) retainFragment.getModel(new Model.Creator() {
				@Override
				public Model createNewModel() {
					return new GitHubAuthorizationModel(TwoFactorAuthActivity.this);
				}
			});

			mGitHubAuthorizationModel.setLoginListener(new GitHubAuthorizationModel.LoginListener() {
				@Override
				public void needTwoFactorAuth() {
					//cannot happen
				}

				@Override
				public void onLoginSuccess(User user) {
					pdProgressDialog.dismiss();
					setResultAndFinish(user);
				}

				@Override
				public void onLoginError() {
					pdProgressDialog.dismiss();
					Toast.makeText(TwoFactorAuthActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onInvalidLoginPassword() {
					//cannot happen
				}

				@Override
				public void onTokenAlreadyExists() {
					pdProgressDialog.dismiss();
					new DeleteAuthorizationWarningDialog().show(getSupportFragmentManager(), "WARNING_DIALOG");
				}

				@Override
				public void onWrongOtpCode() {
					pdProgressDialog.dismiss();
					Toast.makeText(TwoFactorAuthActivity.this, R.string.wrong_otp_code, Toast.LENGTH_SHORT).show();
				}
			});
			mGitHubAuthorizationModel.loginTwoFactor(mUsername, mPassword, otpCode);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(getOtpCodeReceiver(), new IntentFilter(OTP_CODE_RECEIVED));
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(getOtpCodeReceiver());
		super.onPause();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_login_activity, menu);
		mLoginItem = menu.findItem(R.id.action_log_in);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_log_in:
				handleLogin();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onOkToFinish() {
		finish();
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGitHubAuthorizationModel != null) {
			mGitHubAuthorizationModel.clearLoginListener();
			if (isFinishing()) {
				mGitHubAuthorizationModel.stopLoggingIn();
			}
		}
		if (pdProgressDialog != null) {
			pdProgressDialog.dismiss();
			pdProgressDialog = null;
		}
	}

	private BroadcastReceiver getOtpCodeReceiver() {
		if (mOtpCodeReceiver == null) {
			mOtpCodeReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String otpCode = intent.getStringExtra(EXTRA_OTP_CODE);
					etOtpCodeText.setText(otpCode);
					etOtpCodeText.setSelection(etOtpCodeText.getText().length());
				}
			};
		}
		return mOtpCodeReceiver;
	}

	private void setWatcherForOtp() {
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable gitDirEditText) {
				updateEnablement();
			}
		};
		etOtpCodeText.addTextChangedListener(watcher);
	}

	private void updateEnablement() {
		if (mLoginItem != null) {
			mLoginItem.setEnabled(loginEnabled());
		}
	}

	private boolean loginEnabled() {
		Editable otpCode = etOtpCodeText.getText();
		return !TextUtils.isEmpty(otpCode) && otpCode.length() == GIT_HUB_OTP_CODE_LENGTH;
	}

	private void setResultAndFinish(User user) {
		Intent i = new Intent().putExtra(AuthorizationActivity.USER_EXTRA, user);
		setResult(RESULT_OK, i);
		finish();
	}

	public static void startForResult(Activity activity, String username, String password) {
		Intent intent = new Intent(activity, TwoFactorAuthActivity.class);
		intent.putExtra(PARAM_USERNAME, username);
		intent.putExtra(PARAM_PASSWORD, password);
		activity.startActivityForResult(intent, 0);
	}
}
