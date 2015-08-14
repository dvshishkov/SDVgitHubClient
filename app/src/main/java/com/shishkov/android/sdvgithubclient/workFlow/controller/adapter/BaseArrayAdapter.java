package com.shishkov.android.sdvgithubclient.workFlow.controller.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import java.util.List;

public class BaseArrayAdapter<T> extends ArrayAdapter<T> {
	private List<T> mList;
	private LayoutInflater inflater;

	public BaseArrayAdapter(Activity context, List<T> list) {
		super(context, 0);
		this.mList = list;
		inflater = LayoutInflater.from(getContext());

	}

	public void setItems(List<T> list) {
		this.mList = list;
		notifyDataSetChanged();
	}

	@Override
	public T getItem(int position) {
		return mList.get(position);
	}

	@Override
	public int getCount() {
		return mList != null ? mList.size() : 0;
	}
}
