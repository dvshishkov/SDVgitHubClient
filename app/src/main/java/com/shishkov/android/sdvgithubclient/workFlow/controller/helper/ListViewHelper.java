package com.shishkov.android.sdvgithubclient.workFlow.controller.helper;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;

public class ListViewHelper {

	public static StateHolder saveState(ListView listView) {
		if (listView == null) {
			return null;
		}
		StateHolder stateHolder = new StateHolder();
		stateHolder.index = listView.getFirstVisiblePosition();
		View v = listView.getChildAt(0);
		stateHolder.top = v == null ? 0 : v.getTop();
		return stateHolder;
	}

	public static void restoreState(ListView listView, StateHolder stateHolder) {
		if (stateHolder != null) {
			listView.setSelectionFromTop(stateHolder.index, stateHolder.top);
		}
	}

	public static class StateHolder implements Parcelable {
		public int index;
		public int top;

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(this.index);
			dest.writeInt(this.top);
		}

		public StateHolder() {
		}

		private StateHolder(Parcel in) {
			this.index = in.readInt();
			this.top = in.readInt();
		}

		public static final Parcelable.Creator<StateHolder> CREATOR = new Parcelable.Creator<StateHolder>() {
			public StateHolder createFromParcel(Parcel source) {
				return new StateHolder(source);
			}

			public StateHolder[] newArray(int size) {
				return new StateHolder[size];
			}
		};
	}

}