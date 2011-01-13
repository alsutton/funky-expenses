package com.funkyandroid.banking.android.data.listeners;

import android.os.Build;

public class DataChangeListenerFactory {

	public static final DataChangeListener listener;
	static {
		if(Build.VERSION.SDK_INT > 7) {
			listener = new FroyoDataChangeListener();
		} else {
			listener = new DefaultDataChangeListener();
		}
	}
}
