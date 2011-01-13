package com.funkyandroid.banking.android.data.listeners;

import com.funkyandroid.banking.android.data.SettingsManager;

import android.database.sqlite.SQLiteDatabase;

public class DefaultDataChangeListener implements DataChangeListener {

	/**
	 * Default data change listener just updates the last change time
	 */
	public void onDataChanged(final SQLiteDatabase writableDB) {
		SettingsManager.set(writableDB, "LAST_UPDATE", Long.toString(System.currentTimeMillis()));
	}

}
