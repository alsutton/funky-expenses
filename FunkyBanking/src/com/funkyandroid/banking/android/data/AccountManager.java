package com.funkyandroid.banking.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AccountManager {
	 
	private static final String[] COLUMNS = { "_id", "name", "opening_balance", "balance", "currency" };
	
	private static final String[] BALANCE_UPDATE_COLUMNS = { "balance" }; 
	

	/**
	 * The where clause for fetching an individual account.
	 */
	
	private static final String GET_BY_ID_SQL = "_id = ?";
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static Cursor getAll(final SQLiteDatabase db) {
		return db.query(DBHelper.ACCOUNTS_TABLE_NAME, AccountManager.COLUMNS, 
				null, null, null, null, "name ASC");
	}
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static Account getById(final SQLiteDatabase db,final Integer id) {
		String[] whereValues = { id.toString() };
		Cursor cursor = db.query(DBHelper.ACCOUNTS_TABLE_NAME, AccountManager.COLUMNS, 
				AccountManager.GET_BY_ID_SQL, whereValues, null, null, "name ASC");
		
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
	
	public static synchronized void create(final SQLiteDatabase db, 
			final Account account) {
		ContentValues values = new ContentValues();
		values.put("name", account.getName());
		values.put("balance", account.getOpeningBalance());
		values.put("opening_balance", account.getOpeningBalance());
		values.put("currency", account.getCurrency());
		db.insert(DBHelper.ACCOUNTS_TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized void update(final SQLiteDatabase db, 
			final Account account, long oldOpeningBalance) {
		String[] whereArgs = { Integer.toString(account.getId()) };

		ContentValues values = new ContentValues();
		values.put("name", account.getName());
		values.put("opening_balance", account.getOpeningBalance());
		values.put("currency", account.getCurrency());
		db.update(	DBHelper.ACCOUNTS_TABLE_NAME, 
					values, 
					AccountManager.GET_BY_ID_SQL, 
					whereArgs);
		
		adjustBalance(db, account.getId(), 0 - oldOpeningBalance);
		adjustBalance(db, account.getId(), account.getOpeningBalance() );
	}
	
	/**
	 * Adjust the balance of an account.
	 * 
	 * @param db The database to use to get the account information.
	 * @param id The ID for the account.
	 * @param adjustment The amount to adjust the balance by.
	 */
	
	public static synchronized long adjustBalance(final SQLiteDatabase db,
			final Integer id, final long adjustment) {
		String[] whereArgs = { id.toString() };
		Cursor cursor = db.query(	DBHelper.ACCOUNTS_TABLE_NAME, 
									AccountManager.BALANCE_UPDATE_COLUMNS, 
									AccountManager.GET_BY_ID_SQL, 
									whereArgs, 
									null, 
									null, 
									null);
		long balance;
		try {
			if(! cursor.moveToNext()) {
				throw new AccountNotFoundException();
			}			
			balance = cursor.getLong(0);
		} finally {
			cursor.close();
		}

		balance += adjustment;

		ContentValues values = new ContentValues();
		values.put("balance", balance);
		db.update(	DBHelper.ACCOUNTS_TABLE_NAME, 
					values, 
					AccountManager.GET_BY_ID_SQL, 
					whereArgs);
		
		return balance;
	}

	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static void delete(final SQLiteDatabase db, final Account account) {
		String[] whereValues = { Integer.toString(account.getId()) };
		db.delete(	DBHelper.ACCOUNTS_TABLE_NAME,  
					AccountManager.GET_BY_ID_SQL, 
					whereValues);
		TransactionManager.deleteAllForAccount(db, account);
	}
	
	/**
	 * Exception thrown when updating an account if the original account
	 * is not found.
	 */
	public static class AccountNotFoundException extends RuntimeException {
		/**
		 * Generated serial ID.
		 */
		private static final long serialVersionUID = 7548274085238209560L;

		public AccountNotFoundException() {
			super();
		}
	}
}
