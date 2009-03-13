package com.funkyandroid.banking.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PayeeManager {
	 
	private static final String[] COLUMNS = { "_id", "name" }; 

	/**
	 * The where clause for fetching an individual account.
	 */
	
	private static final String GET_BY_ID_SQL = "_id = ?";
	
	/**
	 * The where clause for fetching an individual payee by their name.
	 */
	
	private static final String GET_BY_NAME_SQL = "name = ?";
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static Integer getId(final SQLiteDatabase db, String name) {
		String[] whereArgs = { name };
		
		final Cursor cursor = db.query(	DBHelper.PAYEE_TABLE_NAME, 
										PayeeManager.COLUMNS, 
										PayeeManager.GET_BY_NAME_SQL, 
										whereArgs, 
										null, 
										null,
										null); 
		try {
			if(cursor.moveToNext()) {
				return cursor.getInt(0);
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
	
	public static String getName(final SQLiteDatabase db,final Integer id) {
		String[] whereValues = { id.toString() };
		Cursor cursor = db.query(
				DBHelper.PAYEE_TABLE_NAME, 
				PayeeManager.COLUMNS, 
				PayeeManager.GET_BY_ID_SQL, 
				whereValues, 
				null, 
				null, 
				null);
		
		try {
			if(cursor.moveToNext()) {
				return cursor.getString(1);
			}			
			return null;
		} finally {
			cursor.close();
		}
	}
	
	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized Integer create(final SQLiteDatabase db, 
			final String name) {
		ContentValues values = new ContentValues();
		values.put("name", name);
		db.insert(	DBHelper.PAYEE_TABLE_NAME, 
					null, 
					values);
		
		return getId(db, name);
	}
	
	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized void update(final SQLiteDatabase db, 
			final Integer id, final String name) {
		String[] whereArgs = { Integer.toString(id) };

		ContentValues values = new ContentValues();
		values.put("name", name);
		db.update(	DBHelper.PAYEE_TABLE_NAME, 
					values, 
					PayeeManager.GET_BY_ID_SQL, 
					whereArgs);
	}
}
