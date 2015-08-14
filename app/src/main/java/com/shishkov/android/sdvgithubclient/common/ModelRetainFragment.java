package com.shishkov.android.sdvgithubclient.common;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class ModelRetainFragment extends Fragment {

	public static final String TAG = "ModelRetainFragment";

	private Model model;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	public Model getModel(Model.Creator creator) {
		if (model == null) {
			model = creator.createNewModel();
		}
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public static ModelRetainFragment getRetainFragment(AppCompatActivity activity) {
		ModelRetainFragment frag = (ModelRetainFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (frag == null) {
			frag = new ModelRetainFragment();
			activity.getSupportFragmentManager().beginTransaction().add(frag, TAG).commit();
		}
		return frag;
	}
}