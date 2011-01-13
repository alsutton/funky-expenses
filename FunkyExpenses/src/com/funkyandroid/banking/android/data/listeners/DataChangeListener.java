package com.funkyandroid.banking.android.data.listeners;

import android.database.sqlite.SQLiteDatabase;

/**
 * Interface for objects which react to changes in the database
 */
public interface DataChangeListener {

	/**
	 * Called whenever data is changed.
	 */
	public void onDataChanged(final SQLiteDatabase writableDB);
}
