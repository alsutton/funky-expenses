package com.funkyandroid.banking.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AccountManager {
	 
	/**
	 * The query to get information about all of the accounts
	 */
	
	private static final String GET_ALL_QUERY = 
			"SELECT a._id, a.name, a.opening_balance, a.balance, c.symbol "+
			"  FROM "+DBHelper.ACCOUNTS_TABLE_NAME+" a, "+DBHelper.CURRENCIES_TABLE_NAME+" c "+
			" WHERE a.currency = c.short_code ORDER BY name ASC";
	
	private static final String[] COLUMNS = { "_id", "name", "opening_balance", "balance", "currency" };
	
	private static final String[] BALANCE_UPDATE_COLUMNS = { "balance" }; 
	
	private static final String[] BALANCE_COLS = { "balance" }; 

	/**
	 * The where clause for fetching an individual account.
	 */
	
	private static final String GET_BY_ID_SQL = "_id = ?";
	
	/**
	 * The column holding a payee name.
	 */
	public static final String[] NAME_COL = { "name" };
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static Cursor getAll(final SQLiteDatabase db) {
		return db.rawQuery(GET_ALL_QUERY, null); 
	}
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static Account getById(final SQLiteDatabase db,final int id) {
		String[] whereValues = { Integer.toString( id ) };
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
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static long getBalanceById(final SQLiteDatabase db,final int id) {
		String[] whereValues = { Integer.toString(id) };
		Cursor cursor = db.query(DBHelper.ACCOUNTS_TABLE_NAME, AccountManager.BALANCE_COLS, 
				AccountManager.GET_BY_ID_SQL, whereValues, null, null, null);
		
		try {
			if(cursor.moveToNext()) {
				return cursor.getLong(0);
			}			
			return 0;
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
		values.put("name", account.name);
		values.put("balance", account.openingBalance);
		values.put("opening_balance", account.openingBalance);
		values.put("currency", account.currency);
		db.insert(DBHelper.ACCOUNTS_TABLE_NAME, null, values);
	}
	
	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized void update(final SQLiteDatabase db, 
			final Account account, long oldOpeningBalance) {
		String[] whereArgs = { Integer.toString(account.id) };

		ContentValues values = new ContentValues();
		values.put("name", account.name);
		values.put("opening_balance", account.openingBalance);
		values.put("currency", account.currency);
		db.update(	DBHelper.ACCOUNTS_TABLE_NAME, 
					values, 
					AccountManager.GET_BY_ID_SQL, 
					whereArgs);
		
		adjustBalance(db, account.id, 0 - oldOpeningBalance);
		adjustBalance(db, account.id, account.openingBalance );
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
		String[] whereValues = { Integer.toString(account.id) };
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
