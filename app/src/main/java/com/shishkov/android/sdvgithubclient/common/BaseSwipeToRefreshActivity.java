package com.shishkov.android.sdvgithubclient.common;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.shishkov.android.sdvgithubclient.R;
import com.shishkov.android.sdvgithubclient.utils.AndroidUtils;


public abstract class BaseSwipeToRefreshActivity extends BaseToolbarActivity {

	private static final String KEY_IS_REFRESHING = "KEY_IS_REFRESHING";
	protected SwipeRefreshLayout mSwipeRefreshLayout;
	protected boolean mRefreshing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mRefreshing = savedInstanceState.getBoolean(KEY_IS_REFRESHING);
		}

		AndroidUtils.doInGlobalLayoutListener(this, new Runnable() {
			@Override
			public void run() {
				if (mRefreshing) {
					mSwipeRefreshLayout.setRefreshing(true);
				}
			}
		});
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				setRefreshing(true);
				handleOnRefresh();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_REFRESHING, mRefreshing);
	}

	protected abstract void handleOnRefresh();

	protected void setRefreshing(boolean isRefreshing) {
		mSwipeRefreshLayout.setRefreshing(isRefreshing);
		mRefreshing = isRefreshing;
		mSwipeRefreshLayout.setEnabled(!isRefreshing);
	}


}
