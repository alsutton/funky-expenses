package com.funkyandroid.banking.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CategoryManager {
	 
	private static final String[] ACCOUNT_COLUMNS = { "_id", "name", "balance", "currency" }; 

	/**
	 * The where clause for fetching an individual account.
	 */
	
	private static final String GET_BY_ID_SQL = "_id = ?";
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static Cursor getAccounts(final SQLiteDatabase db) {
		return db.query(DBHelper.ACCOUNTS_TABLE_NAME, CategoryManager.ACCOUNT_COLUMNS, 
				null, null, null, null, "name ASC");
	}
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static Account getAccount(final SQLiteDatabase db,final Integer id) {
		String[] whereValues = { id.toString() };
		Cursor cursor = db.query(DBHelper.ACCOUNTS_TABLE_NAME, CategoryManager.ACCOUNT_COLUMNS, 
				CategoryManager.GET_BY_ID_SQL, whereValues, null, null, "name ASC");
		
		try {
			if(cursor.moveToNext()) {
				return new Account(cursor);
			}			
			return null;
		} finally {
			cursor.close();
		}
	}
	
	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized void createAccount(final SQLiteDatabase db, 
			final Account account) {
		ContentValues values = new ContentValues();
		values.put("name", account.getName());
		values.put("balance", account.getBalance());
		values.put("currency", account.getCurrency());
		db.insert(DBHelper.ACCOUNTS_TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized void updateAccount(final SQLiteDatabase db, 
			final Account account) {
		String[] whereArgs = { Integer.toString(account.getId()) };

		ContentValues values = new ContentValues();
		values.put("name", account.getName());
		values.put("balance", account.getBalance());
		values.put("currency", account.getCurrency());
		db.update(	DBHelper.ACCOUNTS_TABLE_NAME, 
					values, 
					CategoryManager.GET_BY_ID_SQL, 
					whereArgs);
	}
}
