package com.shishkov.android.sdvgithubclient.utils.callbacks;

public interface TaskCallback<T> {
	void onSuccess(T t);
	void onError();
}
