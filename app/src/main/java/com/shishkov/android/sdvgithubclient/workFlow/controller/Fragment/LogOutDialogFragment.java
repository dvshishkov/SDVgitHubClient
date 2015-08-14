package com.shishkov.android.sdvgithubclient.workFlow.controller.Fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.shishkov.android.sdvgithubclient.R;


public class LogOutDialogFragment extends DialogFragment {

	public interface YesNoListener {
		void onLogoutYes();

		void onLogoutNo();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof YesNoListener)) {
			throw new ClassCastException(activity.toString() + " must implement YesNoListener");
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.log_out_dialog_title)
				.setMessage(R.string.log_out_dialog_message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						((YesNoListener) getActivity()).onLogoutYes();
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						((YesNoListener) getActivity()).onLogoutNo();
					}
				})
				.create();
	}
}