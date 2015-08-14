package com.shishkov.android.sdvgithubclient.workFlow.controller.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shishkov.android.sdvgithubclient.R;
import com.shishkov.android.sdvgithubclient.authorization.controller.Activity.AuthorizationActivity;
import com.shishkov.android.sdvgithubclient.common.BaseSwipeToRefreshActivity;
import com.shishkov.android.sdvgithubclient.common.Model;
import com.shishkov.android.sdvgithubclient.common.ModelRetainFragment;
import com.shishkov.android.sdvgithubclient.utils.AccountUtils;
import com.shishkov.android.sdvgithubclient.utils.ConnectionCheck;
import com.shishkov.android.sdvgithubclient.utils.callbacks.TaskCallback;
import com.shishkov.android.sdvgithubclient.workFlow.controller.Fragment.LogOutDialogFragment;
import com.shishkov.android.sdvgithubclient.workFlow.controller.adapter.ReposListAdapter;
import com.shishkov.android.sdvgithubclient.workFlow.controller.helper.ListViewHelper;
import com.shishkov.android.sdvgithubclient.workFlow.model.Cache;
import com.shishkov.android.sdvgithubclient.workFlow.model.GitHubDataRequestModel;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import java.util.ArrayList;
import java.util.List;

public class RepositoriesActivity extends BaseSwipeToRefreshActivity implements LogOutDialogFragment.YesNoListener {

	private static final String TAG = "RepositoriesActivity";
	private static final String LIST_VIEW_STATE = "state";
	private static final String KEY_ZERO_DATA_WITH_MESSAGE = "KEY_ZERO_DATA_WITH_MESSAGE";

	private List<Repository> mReposList;
	private ReposListAdapter mReposListAdapter;

	private GitHubDataRequestModel mGitHubDataRequestModel;
	private User mUser;

	private ListView lvRepositories;
	private FrameLayout flReposProgressBar;
	private View vZeroDataHeader;
	private TextView tvZeroDataText;
	private String mZeroDataWithMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repositories_list);

		mUser = (User) getIntent().getSerializableExtra(AuthorizationActivity.USER_EXTRA);

		lvRepositories = (ListView) findViewById(R.id.repos_list_view);
		flReposProgressBar = (FrameLayout) findViewById(R.id.repos_progressBar);

		setupHeader();
		mReposListAdapter = new ReposListAdapter(this, mReposList);
		lvRepositories.setAdapter(mReposListAdapter);

		lvRepositories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position -= lvRepositories.getHeaderViewsCount();
				CommitsActivity.start(RepositoriesActivity.this, mReposList.get(position));
			}
		});

		if (savedInstanceState != null) {
			mZeroDataWithMessage = savedInstanceState.getString(KEY_ZERO_DATA_WITH_MESSAGE);
		}

		setData();
	}


	/**
	 * Need header, because swipe to refresh requires only 1 scrollable child
	 */
	private void setupHeader() {
		View header = LayoutInflater.from(this).inflate(R.layout.view_repos_list_header, null);
		vZeroDataHeader = header.findViewById(R.id.zero_data_frame);
		hideZeroData();
		tvZeroDataText = (TextView) header.findViewById(R.id.zero_data_text);
		lvRepositories.addHeaderView(header, null, false);
	}

	private void setData() {
		ModelRetainFragment retainFragment = ModelRetainFragment.getRetainFragment(this);
		mGitHubDataRequestModel = (GitHubDataRequestModel) retainFragment.getModel(new Model.Creator() {
			@Override
			public Model createNewModel() {
				return new GitHubDataRequestModel(RepositoriesActivity.this);
			}
		});
		connectToModel();
		if (mZeroDataWithMessage != null) {
			showZeroData(mZeroDataWithMessage);
			return;
		}
		if (Cache.getInstance().getRepositories() == null) {
			loadRepos();
		} else {
			if (Cache.getInstance().getRepositories().isEmpty()) {
				showZeroData(getString(R.string.zero_data_repos));
			} else {
				mReposList = Cache.getInstance().getRepositories();
				mReposListAdapter.setItems(mReposList);
				getSupportActionBar().setTitle(mUser.getLogin() + "`s repos");
			}
		}
	}


	private void connectToModel() {
		Log.e(TAG, "connectToModel");
		mGitHubDataRequestModel.setLoadRepositoriesListener(new GitHubDataRequestModel.DataRequestListener<List<Repository>>() {
			@Override
			public void onPreRequest() {
				if (mRefreshing) {
					return;
				}
				mSwipeRefreshLayout.setEnabled(false);
				hideZeroData();
				flReposProgressBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onRequestSuccess(List<Repository> repositories) {
				Log.e(TAG, "onRequestSuccess");
				setRefreshing(false);
				flReposProgressBar.setVisibility(View.GONE);
				mReposList = repositories;
				Cache.getInstance().setRepositories(mReposList);
				getSupportActionBar().setTitle(mUser.getLogin() + "`s repos");
				mReposListAdapter.setItems(mReposList);
				hideZeroData();
			}

			/**
			 * cannot happen, because GitHub does not throw 409 error
			 * if there is no repositories (as it does for commits!),
			 * so we cannot know if there is an error downloading user`s repositories or he doesn't have any
			 */
			@Override
			public void onEmptyData() {
			}

			@Override
			public void onRequestError() {
				setRefreshing(false);
				flReposProgressBar.setVisibility(View.GONE);
				Cache.getInstance().setRepositories(new ArrayList<Repository>());
				showZeroData(getString(R.string.zero_data_repos));
			}
		});
	}

	private void loadRepos() {
		Log.e(TAG, "loadRepos");
		if (!ConnectionCheck.isOnline(this)) {
			if (mRefreshing) {
				Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show();
			} else {
				showZeroData(getString(R.string.offline));
			}
			setRefreshing(false);
		} else {
			mGitHubDataRequestModel.loadRepositories();
		}
	}

	@Override
	protected void handleOnRefresh() {
		loadRepos();
	}


	private void showZeroData(String message) {
		vZeroDataHeader.setVisibility(View.VISIBLE);
		tvZeroDataText.setText(message);
		mZeroDataWithMessage = message;
		mReposListAdapter.setItems(null);
	}

	private void hideZeroData() {
		vZeroDataHeader.setVisibility(View.GONE);
		mZeroDataWithMessage = null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ListViewHelper.StateHolder state = ListViewHelper.saveState(lvRepositories);
		outState.putParcelable(LIST_VIEW_STATE, state);
		outState.putString(KEY_ZERO_DATA_WITH_MESSAGE, mZeroDataWithMessage);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		ListViewHelper.StateHolder state = savedInstanceState.getParcelable(LIST_VIEW_STATE);
		ListViewHelper.restoreState(lvRepositories, state);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_repos_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_log_out:
				new LogOutDialogFragment().show(getSupportFragmentManager(), "LOGOUT_DIALOG");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onLogoutYes() {
		AccountManager accountManager = AccountManager.get(this);
		Account account = AccountUtils.getAccount(accountManager);

		AccountUtils.removeAccount(accountManager, account, new TaskCallback<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Cache.getInstance().cleanCache();
				AuthorizationActivity.start(RepositoriesActivity.this);
			}

			@Override
			public void onError() {
				Toast.makeText(RepositoriesActivity.this, R.string.unable_remove_account, Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public void onLogoutNo() {
		//do nothing
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGitHubDataRequestModel != null) {
			mGitHubDataRequestModel.clearLoadRepositoriesListener();
			if (isFinishing()) {
				mGitHubDataRequestModel.stopLoadRepositories();
			}
		}
	}

	public static void start(Activity activity, User user) {
		Intent intent = new Intent(activity, RepositoriesActivity.class);
		intent.putExtra(AuthorizationActivity.USER_EXTRA, user);
		activity.startActivity(intent);
	}
}
