package com.shishkov.android.sdvgithubclient.workFlow.controller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shishkov.android.sdvgithubclient.R;
import com.shishkov.android.sdvgithubclient.common.BaseSwipeToRefreshActivity;
import com.shishkov.android.sdvgithubclient.common.BaseToolbarActivity;
import com.shishkov.android.sdvgithubclient.common.Model;
import com.shishkov.android.sdvgithubclient.common.ModelRetainFragment;
import com.shishkov.android.sdvgithubclient.utils.AndroidUtils;
import com.shishkov.android.sdvgithubclient.utils.ConnectionCheck;
import com.shishkov.android.sdvgithubclient.workFlow.controller.adapter.CommitListAdapter;
import com.shishkov.android.sdvgithubclient.workFlow.controller.helper.ListViewHelper;
import com.shishkov.android.sdvgithubclient.workFlow.model.Cache;
import com.shishkov.android.sdvgithubclient.workFlow.model.GitHubDataRequestModel;
import com.squareup.picasso.Picasso;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CommitsActivity extends BaseSwipeToRefreshActivity {

	private static final String LIST_VIEW_STATE = "LIST_VIEW_STATE";
	private static final String KEY_REPOSITORY = "KEY_REPOSITORY";
	private static final String KEY_ZERO_DATA_WITH_MESSAGE = "KEY_ZERO_DATA_WITH_MESSAGE";

	private List<RepositoryCommit> mCommits;
	private CommitListAdapter mCommitListAdapter;
	private Repository mRepository;

	private GitHubDataRequestModel mGitHubDataRequestModel;

	private ListView lvCommits;
	private TextView tvAuthorName;
	private ImageView ivAuthorAvatar;
	private TextView tvNumOfForks;
	private TextView tvNumOfWatches;
	private TextView tvDesctiption;
	private TextView tvAvatarZeroData;
	private FrameLayout flZeroData;
	private TextView tvZeroDataText;
	private FrameLayout flCommitsProgressBar;
	private String mZeroDataWithMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_commits_list);
		mRepository = (Repository) getIntent().getSerializableExtra(KEY_REPOSITORY);

		lvCommits = (ListView) findViewById(R.id.commits_list_view);

		setupHeader();
		mCommitListAdapter = new CommitListAdapter(this, mCommits);
		lvCommits.setAdapter(mCommitListAdapter);
		if (savedInstanceState != null) {
			mZeroDataWithMessage = savedInstanceState.getString(KEY_ZERO_DATA_WITH_MESSAGE);
		}
		setData();

	}

	private void setupHeader() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(mRepository.getName());
		View header = LayoutInflater.from(this).inflate(R.layout.view_commit_list_header, null);

		tvAuthorName = (TextView) header.findViewById(R.id.author_name);
		ivAuthorAvatar = (ImageView) header.findViewById(R.id.author_avatar);
		tvNumOfForks = (TextView) header.findViewById(R.id.num_of_forks);
		tvNumOfWatches = (TextView) header.findViewById(R.id.num_of_watches);
		tvDesctiption = (TextView) header.findViewById(R.id.repo_description);
		flCommitsProgressBar = (FrameLayout) header.findViewById(R.id.commits_progressBar);
		tvAvatarZeroData = (TextView) findViewById(R.id.zero_data_avatar);

		flZeroData = (FrameLayout) header.findViewById(R.id.zero_data);
		flZeroData.setVisibility(View.GONE);
		tvZeroDataText = (TextView) header.findViewById(R.id.zero_data_text);
		if (mRepository.getOwner().getName() == null) {
			tvAuthorName.setText(mRepository.getOwner().getLogin());
		} else {
			tvAuthorName.setText(mRepository.getOwner().getName());
		}

		if (mRepository.getOwner().getAvatarUrl() == null || mRepository.getOwner().getAvatarUrl().isEmpty()) {
			tvAvatarZeroData.setVisibility(View.VISIBLE);
		} else {
			Picasso.with(this).load(mRepository.getOwner().getAvatarUrl()).into(ivAuthorAvatar);
		}
		tvNumOfForks.setText("forks: " + mRepository.getForks());
		tvNumOfWatches.setText("watches: " + mRepository.getWatchers());
		tvDesctiption.setText(mRepository.getDescription());

		lvCommits.addHeaderView(header, null, false);
	}

	private void setData() {

		ModelRetainFragment retainFragment = ModelRetainFragment.getRetainFragment(this);
		mGitHubDataRequestModel = (GitHubDataRequestModel) retainFragment.getModel(new Model.Creator() {
			@Override
			public Model createNewModel() {
				return new GitHubDataRequestModel(CommitsActivity.this);
			}
		});
		connectToModel();
		if (mZeroDataWithMessage != null) {
			showZeroData(mZeroDataWithMessage);
			return;
		}
		if (checkForCommitsInCache()) {
			mCommits = Cache.getInstance().getCommitsHashMap().get(mRepository.getId());
			if (mCommits.isEmpty()) {
				showZeroData(getString(R.string.no_commits));
			} else {
				mCommitListAdapter.setItems(mCommits);
			}
			return;
		}
		loadCommits();
	}



	private void connectToModel() {
		mGitHubDataRequestModel.setLoadCommitsListener(new GitHubDataRequestModel.DataRequestListener<List<RepositoryCommit>>() {
			@Override
			public void onPreRequest() {
				if (mRefreshing) {
					return;
				}
				mSwipeRefreshLayout.setEnabled(false);
				flZeroData.setVisibility(View.GONE);
				flCommitsProgressBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onRequestSuccess(List<RepositoryCommit> commits) {
				setRefreshing(false);
				mCommits = commits;
				Cache.getInstance().saveRepoCommitsToCache(mRepository.getId(), mCommits);
				flCommitsProgressBar.setVisibility(View.GONE);
				mCommitListAdapter.setItems(mCommits);
				mZeroDataWithMessage = null;
			}

			@Override
			public void onEmptyData() {
				setRefreshing(false);
				Cache.getInstance().saveRepoCommitsToCache(mRepository.getId(), new ArrayList<RepositoryCommit>());
				flCommitsProgressBar.setVisibility(View.GONE);
				showZeroData(getString(R.string.no_commits));

			}

			@Override
			public void onRequestError() {
				setRefreshing(false);
				flCommitsProgressBar.setVisibility(View.GONE);
				showZeroData(getString(R.string.zero_data_commits));

			}
		});
	}

	private void loadCommits() {
		if (!ConnectionCheck.isOnline(this)) {
			if (mRefreshing) {
				Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show();
			} else {
				showZeroData(getString(R.string.offline));
			}
			setRefreshing(false);
		} else {
			mGitHubDataRequestModel.loadCommits(mRepository);
		}
	}

	@Override
	protected void handleOnRefresh() {
		loadCommits();
	}

	private void showZeroData(String message) {
		flZeroData.setVisibility(View.VISIBLE);
		tvZeroDataText.setText(message);
		mZeroDataWithMessage = message;
		mCommitListAdapter.setItems(null);
	}

	private boolean checkForCommitsInCache() {
		LinkedHashMap<Long, List<RepositoryCommit>> commitsHashMap = Cache.getInstance().getCommitsHashMap();
		return commitsHashMap.get(mRepository.getId()) != null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ListViewHelper.StateHolder state = ListViewHelper.saveState(lvCommits);
		outState.putParcelable(LIST_VIEW_STATE, state);
		outState.putString(KEY_ZERO_DATA_WITH_MESSAGE, mZeroDataWithMessage);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		ListViewHelper.StateHolder state = savedInstanceState.getParcelable(LIST_VIEW_STATE);
		ListViewHelper.restoreState(lvCommits, state);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGitHubDataRequestModel != null) {
			mGitHubDataRequestModel.clearLoadCommitsListener();
			if (isFinishing()) {
				mGitHubDataRequestModel.stopLoadCommits();
			}
		}
	}

	public static void start(Activity activity, Repository repository) {
		Intent i = new Intent(activity, CommitsActivity.class);
		i.putExtra(KEY_REPOSITORY, repository);
		activity.startActivity(i);
	}
}
