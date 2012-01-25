package com.funkyandroid.banking.android;

import android.database.sqlite.SQLiteDatabase;

/**
 * Interface for classes which offer a readable database to their fragments.
 *
 * @author Al Sutton
 */

public interface DatabaseReadingActivity {

	/**
	 * Get the readable database
	 */

	public SQLiteDatabase getReadableDatabaseConnection();
}
