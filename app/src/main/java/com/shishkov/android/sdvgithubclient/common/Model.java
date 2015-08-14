package com.shishkov.android.sdvgithubclient.common;

/**
 * Marker interface
 */
public interface Model {

	/**
	 * Callback interface to create new {@link Model}
	 */
	interface Creator {
		Model createNewModel();
	}

}