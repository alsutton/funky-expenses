package com.funkyandroid.banking.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SettingsManager {
	
	/**
	 * The setting name for the password.
	 */
	
	public static final String PASSWORD_SETTING = "password";
	

	private static final String[] COLUMNS = { "value" }; 

	/**
	 * The where clause for fetching an individual payee by their name.
	 */
	
	private static final String GET_BY_NAME_SQL = "name = ?";

	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static String get(final SQLiteDatabase db, String name) {
		String[] whereArgs = { name };
		
		final Cursor cursor = db.query(	DBHelper.SETTINGS_TABLE_NAME, 
										SettingsManager.COLUMNS, 
										SettingsManager.GET_BY_NAME_SQL, 
										whereArgs, 
										null, 
										null,
										null); 
		try {
			if(cursor.moveToNext()) {
				return cursor.getString(0);
			}			
			return null;
		} finally {
			cursor.close();
		}
	}
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static void set(final SQLiteDatabase db, String name, String value) {
		String[] whereArgs = { name };

		boolean exists = (get(db, name) != null);		
		if( value == null || value.length() == 0) {
			if( exists ) {
				db.delete(
						DBHelper.SETTINGS_TABLE_NAME, 
						SettingsManager.GET_BY_NAME_SQL, 
						whereArgs);
			}
		} else {
			if(exists) {
				ContentValues values = new ContentValues();
				values.put("value", value);
				db.update(	DBHelper.SETTINGS_TABLE_NAME, 
							values, 
							SettingsManager.GET_BY_NAME_SQL, 
							whereArgs);
			} else {
				ContentValues values = new ContentValues();
				values.put("name", name);
				values.put("value", value);
				db.insert( DBHelper.SETTINGS_TABLE_NAME, null, values);
			}
		}
	}
}
