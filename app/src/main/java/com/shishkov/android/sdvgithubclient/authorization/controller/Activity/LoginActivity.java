package com.shishkov.android.sdvgithubclient.authorization.controller.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.shishkov.android.sdvgithubclient.R;
import com.shishkov.android.sdvgithubclient.authorization.model.GitHubAuthorizationModel;
import com.shishkov.android.sdvgithubclient.common.BaseToolbarActivity;
import com.shishkov.android.sdvgithubclient.common.Model;
import com.shishkov.android.sdvgithubclient.common.ModelRetainFragment;
import com.shishkov.android.sdvgithubclient.utils.ConnectionCheck;
import com.shishkov.android.sdvgithubclient.utils.ViewUtils;

import org.eclipse.egit.github.core.User;


public class LoginActivity extends BaseToolbarActivity {

	private AutoCompleteTextView actvUserLogin;
	private EditText etPasswordText;
	private CheckBox cbShowPasswordCheckBox;
	private ProgressDialog pdProgressDialog;
	private MenuItem mLoginItem;
	private String mUsername;
	private String mPassword;
	private GitHubAuthorizationModel mGitHubAuthorizationModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		actvUserLogin = (AutoCompleteTextView) findViewById(R.id.view_username);
		etPasswordText = (EditText) findViewById(R.id.view_password);
		cbShowPasswordCheckBox = (CheckBox) findViewById(R.id.show_password_checkBox);
		setWatcherForTextFields();

		cbShowPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if (isChecked) {
					etPasswordText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					etPasswordText.setSelection(etPasswordText.getText().length());
				} else {
					etPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					etPasswordText.setSelection(etPasswordText.getText().length());
				}
			}
		});
	}

	public void handleLogin() {
		if (!ConnectionCheck.isOnline(this)) {
			Toast.makeText(LoginActivity.this, R.string.offline, Toast.LENGTH_SHORT).show();
		} else {
			mUsername = actvUserLogin.getText().toString();
			mPassword = etPasswordText.getText().toString();
			pdProgressDialog = ViewUtils.showProgressDialog(this, getString(R.string.signing_in_please_wait));

			ModelRetainFragment retainFragment = ModelRetainFragment.getRetainFragment(this);
			mGitHubAuthorizationModel = (GitHubAuthorizationModel) retainFragment.getModel(new Model.Creator() {
				@Override
				public Model createNewModel() {
					return new GitHubAuthorizationModel(LoginActivity.this);
				}
			});

			mGitHubAuthorizationModel.setLoginListener(new GitHubAuthorizationModel.LoginListener() {
				@Override
				public void needTwoFactorAuth() {
					pdProgressDialog.dismiss();
					TwoFactorAuthActivity.startForResult(LoginActivity.this, mUsername, mPassword);
				}

				@Override
				public void onLoginSuccess(User user) {
					pdProgressDialog.dismiss();
					setResultAndFinish(user);
				}

				@Override
				public void onLoginError() {
					pdProgressDialog.dismiss();
					Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onInvalidLoginPassword() {
					pdProgressDialog.dismiss();
					Toast.makeText(LoginActivity.this, R.string.wrong_login_or_pass, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onTokenAlreadyExists() {
					//can happen only in Two-Factor authorization
				}

				@Override
				public void onWrongOtpCode() {
					//cannot happen
				}
			});
			mGitHubAuthorizationModel.login(mUsername, mPassword);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
			case RESULT_OK:
				User user = (User) data.getSerializableExtra(AuthorizationActivity.USER_EXTRA);
				setResultAndFinish(user);
				break;
		}
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
			case R.id.action_log_in:
				handleLogin();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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

	private void setWatcherForTextFields() {
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

		actvUserLogin.addTextChangedListener(watcher);
		etPasswordText.addTextChangedListener(watcher);
	}

	private void updateEnablement() {
		if (mLoginItem != null) {
			mLoginItem.setEnabled(loginEnabled());
		}
	}

	private boolean loginEnabled() {
		return !TextUtils.isEmpty(actvUserLogin.getText())
				&& !TextUtils.isEmpty(etPasswordText.getText());
	}

	private void setResultAndFinish(User user) {
		Intent i = new Intent().putExtra(AuthorizationActivity.USER_EXTRA, user);
		setResult(RESULT_OK, i);
		finish();
	}

	public static void startForResult(Activity activity) {
		Intent intent = new Intent(activity, LoginActivity.class);
		activity.startActivityForResult(intent, 0);
	}
}
