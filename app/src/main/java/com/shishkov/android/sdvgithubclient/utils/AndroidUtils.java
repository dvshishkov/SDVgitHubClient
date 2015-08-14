package com.shishkov.android.sdvgithubclient.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

public class AndroidUtils {

	public static void doInGlobalLayoutListener(final Activity activity, final Runnable task) {
		final View v = activity.findViewById(android.R.id.content);
		v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				task.run();
			}
		});

	}


}
