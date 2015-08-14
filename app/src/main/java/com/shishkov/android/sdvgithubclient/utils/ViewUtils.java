package com.shishkov.android.sdvgithubclient.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class ViewUtils {

	public static ProgressDialog showProgressDialog(Context context, String msg) {
		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setMessage(msg);
		dialog.show();
		return dialog;
	}
}
