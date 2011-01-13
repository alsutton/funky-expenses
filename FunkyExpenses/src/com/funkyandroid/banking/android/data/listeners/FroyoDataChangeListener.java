package com.funkyandroid.banking.android.data.listeners;

import android.app.backup.BackupManager;
import android.database.sqlite.SQLiteDatabase;

public class FroyoDataChangeListener extends DefaultDataChangeListener {
	/**
	 * The package name to trigger a backup.
	 */
	
	private static final String PACKAGE_NAME = "com.funkyandroid.banking.android.expenses.adfree";
	
	/**
	 * Default data change listener just updates the last change time
	 */
	public void onDataChanged(final SQLiteDatabase writableDB) {
		super.onDataChanged(writableDB);
		BackupManager.dataChanged(PACKAGE_NAME);
	}

}
