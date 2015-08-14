package com.shishkov.android.sdvgithubclient.workFlow.controller.adapter;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shishkov.android.sdvgithubclient.R;

import org.eclipse.egit.github.core.Repository;

import java.util.List;


public class ReposListAdapter extends BaseArrayAdapter<Repository> {

	private class ViewHolder {
		TextView tvRepoName;
		ImageView ivRepoIcon;
	}

	public ReposListAdapter(Activity context, List<Repository> repositories) {
		super(context, repositories);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.repo_list_item, parent, false);

			holder = new ViewHolder();
			holder.tvRepoName = (TextView) convertView.findViewById(R.id.repo_name);
			holder.ivRepoIcon = (ImageView) convertView.findViewById(R.id.repo_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}


		Repository repository = getItem(position);

		holder.tvRepoName.setText(repository.getName());
		holder.ivRepoIcon.setImageResource(R.drawable.ic_github_repo);

		return convertView;
	}

}
