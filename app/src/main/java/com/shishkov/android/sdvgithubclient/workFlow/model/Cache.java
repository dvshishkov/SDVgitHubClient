package com.shishkov.android.sdvgithubclient.workFlow.model;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;

import java.util.LinkedHashMap;
import java.util.List;

public class Cache {

	private List<Repository> mRepositories;

	private LinkedHashMap<Long, List<RepositoryCommit>> mCommitsHashMap;

	private Cache() {
		mCommitsHashMap = new LinkedHashMap<>();
	}

	private static class SingletonHolder {
		private static final Cache INSTANCE = new Cache();
	}

	public static Cache getInstance() {
		return SingletonHolder.INSTANCE;
	}
	public List<Repository> getRepositories() {
		return mRepositories;
	}

	public void setRepositories(List<Repository> repositories) {
		mRepositories = repositories;
	}

	public LinkedHashMap<Long, List<RepositoryCommit>> getCommitsHashMap() {
		return mCommitsHashMap;
	}

	public void setCommitsHashMap(LinkedHashMap<Long, List<RepositoryCommit>> commitsHashMap) {
		mCommitsHashMap = commitsHashMap;
	}

	public void saveRepoCommitsToCache(Long repId, List<RepositoryCommit> commits) {

		if (mCommitsHashMap.size() >= 10) { // if size too big
			Long eldestKey = mCommitsHashMap.keySet().iterator().next(); // take eldest key
			mCommitsHashMap.remove(eldestKey); // remove eldest key-value pair
		}
		// add new key-value pair
		getCommitsHashMap().put(repId, commits); // Repo + list of commits

	}

	public void cleanCache() {
		mRepositories = null;
		mCommitsHashMap = new LinkedHashMap<>();
	}
}

