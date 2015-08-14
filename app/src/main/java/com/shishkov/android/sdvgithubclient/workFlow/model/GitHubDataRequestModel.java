package com.shishkov.android.sdvgithubclient.workFlow.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.AsyncTask;

import com.shishkov.android.sdvgithubclient.common.Model;
import com.shishkov.android.sdvgithubclient.utils.AccountUtils;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubDataRequestModel implements Model {

	private static final String GITHUB_409_CONFLICT = "409";

	private Activity mActivity;
	private AccountManager mAccountManager;
	private DataRequestListener<List<Repository>> mLoadRepositoriesListener;
	private DataRequestListener<List<RepositoryCommit>> mLoadCommitsListener;
	private AsyncTask mLoadRepositoriesTask;
	private AsyncTask mLoadCommitsTask;
	private boolean mWorking;


	public GitHubDataRequestModel(Activity activity) {
		mActivity = activity;
		mAccountManager = AccountManager.get(activity);
	}


	public void setLoadRepositoriesListener(DataRequestListener<List<Repository>> listener) {
		mLoadRepositoriesListener = listener;
		if (mWorking) {
			mLoadRepositoriesListener.onPreRequest();
		}
	}

	public void stopLoadRepositories() {
		if (mWorking) {
			mLoadRepositoriesTask.cancel(true);
			mWorking = false;
		}
	}

	public void clearLoadRepositoriesListener() {
		mLoadRepositoriesListener = null;
	}


	public void loadRepositories() {
		if (mWorking) {
			return;
		}
		mWorking = true;

		mLoadRepositoriesListener.onPreRequest();

		mLoadRepositoriesTask = new AsyncTask<Void, Void, List<Repository>>() {

			@Override
			protected List<Repository> doInBackground(Void... params) {

				Account account = AccountUtils.getAccount(mAccountManager);
				String authToken = AccountUtils.getAuthToken(mActivity, account, mAccountManager);

				GitHubClient client = new GitHubClient();
				client.setOAuth2Token(authToken);

				RepositoryService service = new RepositoryService(client);
				try {
					return service.getRepositories();
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(List<Repository> repositories) {
				mWorking = false;
				if (repositories == null || repositories.isEmpty()) {
					mLoadRepositoriesListener.onRequestError();
				} else {
					mLoadRepositoriesListener.onRequestSuccess(repositories);
				}
			}
		}.execute();
	}


	public void setLoadCommitsListener(DataRequestListener<List<RepositoryCommit>> listener) {
		mLoadCommitsListener = listener;
		if (mWorking) {
			mLoadCommitsListener.onPreRequest();
		}
	}

	public void stopLoadCommits() {
		if (mWorking) {
			mLoadCommitsTask.cancel(true);
			mWorking = false;
		}
	}

	public void clearLoadCommitsListener() {
		mLoadCommitsListener = null;
	}

	public void loadCommits(final Repository repository) {
		if (mWorking) {
			return;
		}
		mWorking = true;

		mLoadCommitsListener.onPreRequest();

		mLoadCommitsTask = new AsyncTask<Void, Void, List<RepositoryCommit>>() {

			@Override
			protected List<RepositoryCommit> doInBackground(Void... params) {

				Account account = AccountUtils.getAccount(mAccountManager);
				String authToken = AccountUtils.getAuthToken(mActivity, account, mAccountManager);

				GitHubClient client = new GitHubClient();
				client.setOAuth2Token(authToken);

				CommitService commitService = new CommitService(client);
				try {
					return commitService.getCommits(repository);
				} catch (IOException e) {
					e.printStackTrace();
					if (e.getMessage().contains(GITHUB_409_CONFLICT)) {
						return new ArrayList<>();
					}
					return null;
				}
			}

			@Override
			protected void onPostExecute(List<RepositoryCommit> commits) {
				mWorking = false;
				if (commits == null) {
					mLoadCommitsListener.onRequestError();
				} else if (commits.isEmpty()) {
					mLoadCommitsListener.onEmptyData();
				} else {
					mLoadCommitsListener.onRequestSuccess(commits);
				}
			}
		}.execute();
	}


	public interface DataRequestListener<T> {
		void onPreRequest();

		void onRequestSuccess(T t);

		void onEmptyData();

		void onRequestError();
	}

}

