package com.shishkov.android.sdvgithubclient.authorization.controller.Fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.shishkov.android.sdvgithubclient.R;


public class DeleteAuthorizationWarningDialog extends DialogFragment {

	public interface YesToFinishListener {
		void onOkToFinish();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof YesToFinishListener)) {
			throw new ClassCastException(activity.toString() + " must implement YesNoListener");
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.delete_auth_warning_dialog_title)
				.setMessage(R.string.delete_auth_warning_dialog_message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						((YesToFinishListener) getActivity()).onOkToFinish();
					}
				})
				.create();
	}
}