package com.shishkov.android.sdvgithubclient.common;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.shishkov.android.sdvgithubclient.R;


public class BaseToolbarActivity extends AppCompatActivity {

	protected Toolbar toolbar;

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}
}
