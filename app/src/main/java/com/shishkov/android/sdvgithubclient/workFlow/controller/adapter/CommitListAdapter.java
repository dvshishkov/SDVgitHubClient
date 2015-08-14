package com.shishkov.android.sdvgithubclient.workFlow.controller.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shishkov.android.sdvgithubclient.R;

import org.eclipse.egit.github.core.RepositoryCommit;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommitListAdapter extends BaseArrayAdapter<RepositoryCommit> {

	private static class ViewHolder {
		TextView tvCommitMessage;
		TextView tvCommitAuthor;
		TextView tvCommitDate;
		TextView tvCommitHash;



	}


	public CommitListAdapter(Activity context, List<RepositoryCommit> repositories) {
		super(context, repositories);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.commit_list_item, parent, false);

			holder = new ViewHolder();
			holder.tvCommitMessage = (TextView) convertView.findViewById(R.id.commit_message);
			holder.tvCommitAuthor = (TextView) convertView.findViewById(R.id.commit_author);
			holder.tvCommitDate = (TextView) convertView.findViewById(R.id.commit_date);
			holder.tvCommitHash = (TextView) convertView.findViewById(R.id.commit_hash);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}


		RepositoryCommit commit = getItem(position);

		holder.tvCommitMessage.setText(commit.getCommit().getMessage());
		if (commit.getCommitter() == null) {
			if (commit.getAuthor() == null) {
				holder.tvCommitAuthor.setText("");
			} else {
				holder.tvCommitAuthor.setText(commit.getAuthor().getLogin());
			}
		} else {
			if (commit.getCommitter().getLogin() == null) {
				if (commit.getAuthor().getLogin() == null) {
					holder.tvCommitAuthor.setText(commit.getCommit().getCommitter().getName());
				} else {
					holder.tvCommitAuthor.setText(commit.getAuthor().getLogin());
				}
			} else {
				holder.tvCommitAuthor.setText(commit.getCommitter().getLogin());
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
		holder.tvCommitDate.setText("   authored on   " + sdf.format(commit.getCommit().getCommitter().getDate()));
		holder.tvCommitHash.setText("(" + commit.getSha() + ")");

		convertView.setOnClickListener(null);
		return convertView;
	}
}
