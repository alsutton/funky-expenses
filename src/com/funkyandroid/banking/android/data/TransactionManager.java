package com.funkyandroid.banking.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public final class TransactionManager {
	 
	private static final String[] COLUMNS = { 
		"_id", "account_id", "timestamp", "category_id",
		"payee_id", "type", "amount" /*, "recipient_account_id"*/ };

	/**
	 * The SQL to select the transactions for an account.
	 */
	
	private static final String TRANSACTIONS_FOR_ACCOUNT_SQL = 
		"SELECT t._id as _id, p.name, t.amount, t.timestamp FROM "
		+ DBHelper.ENTRIES_TABLE_NAME
		+ " t, "
		+ DBHelper.PAYEE_TABLE_NAME
		+ " p WHERE t.account_id = ? AND p._id = t.payee_id ORDER BY timestamp DESC";

	/**
	 * The SQL to select the transactions for an account.
	 */
	
	private static final String EXPORT_TRANSACTIONS_FOR_ACCOUNT_SQL = 
		"SELECT t.timestamp, c.name, p.name, t.amount FROM "
		+ DBHelper.ENTRIES_TABLE_NAME
		+ " t, "
		+ DBHelper.PAYEE_TABLE_NAME
		+ " p, "
		+ DBHelper.CATEGORIES_TABLE_NAME
		+" c WHERE t.account_id = ? AND p._id = t.payee_id AND c._id = t.category_id ORDER BY timestamp DESC";

	/**
	 * The SQL to select the transactions for an account.
	 */
	
	private static final String ACCOUNT_AND_CAT_QUERY = 
		"SELECT t._id as _id, p.name, t.amount, t.timestamp FROM "
		+ DBHelper.ENTRIES_TABLE_NAME
		+ " t, "
		+ DBHelper.PAYEE_TABLE_NAME
		+ " p WHERE t.account_id = ? AND t.category_id = ? AND p._id = t.payee_id ORDER BY timestamp DESC";

	/**
	 * The where clause for fetching an individual transaction.
	 */
	
	private static final String GET_BY_ID_SQL = "_id = ?";

	/**
	 * The where clause for fetching an individual transaction.
	 */
	
	private static final String DELETE_FOR_ACCOUNT_SQL = "account_id = ?";
	
	/**
	 * Get the list of transactions for an account.
	 * 
	 * @param db database to query.
	 * @param accountId The ID of the account to get the transactions for.
	 */
	
	public static Cursor getForAccount(final SQLiteDatabase db, 
			final int accountId) {		
		String[] whereValues = { Integer.toString(accountId) };
		return db.rawQuery (TRANSACTIONS_FOR_ACCOUNT_SQL, whereValues);
	}

	/**
	 * Get the list of transactions for an account.
	 * 
	 * @param db database to query.
	 * @param accountId The ID of the account to get the transactions for.
	 */
	
	public static Cursor getForExportForAccount(final SQLiteDatabase db, 
			final int accountId) {		
		String[] whereValues = { Integer.toString(accountId) };
		return db.rawQuery (EXPORT_TRANSACTIONS_FOR_ACCOUNT_SQL, whereValues);
	}

	/**
	 * Get an cursor pointing to the entries with the given category ID in an account with the given ID.
	 *  
	 * @param accountId The account to get the entries from.
	 * @param categoryId The ID of the category to get the entries for.
	 * 
	 * @return The cursor.
	 */
	public static Cursor getCursorForCategoryAndAccount(final SQLiteDatabase db, 
			final int accountId, final int categoryId) {
		String[] whereValues = { Integer.toString( accountId ), Integer.toString( categoryId ) };
		return db.rawQuery (ACCOUNT_AND_CAT_QUERY, whereValues);
	}
	
	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static Transaction getById(final SQLiteDatabase db,final int id) {
		String[] whereValues = { Integer.toString(id) };
		Cursor cursor = db.query(DBHelper.ENTRIES_TABLE_NAME, TransactionManager.COLUMNS, 
				TransactionManager.GET_BY_ID_SQL, whereValues, null, null, "timestamp DESC");
		
		Transaction transaction;
		try {
			if(!cursor.moveToNext()) {
				return null;
			}			

			transaction = new Transaction(cursor);
		} finally {
			cursor.close();
		}
		
		transaction.setPayee(
				PayeeManager.getName(db, transaction.getPayeeId())
			);
		return transaction;
	}
	
	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized long  create(final SQLiteDatabase db, 
			final Transaction transaction) {
		Integer payeeId = PayeeManager.getId(db, transaction.getPayee());
		if( payeeId == null ) {
			payeeId = PayeeManager.create(db, transaction.getPayee());
		}
		
		ContentValues values = new ContentValues();
		values.put("amount", transaction.getAmount());
		values.put("account_id", transaction.getAccountId());
		values.put("payee_id", payeeId);
		values.put("category_id", transaction.getCategoryId());
		values.put("link_id", 0);
		values.put("timestamp", transaction.getTimestamp());
		values.put("type", transaction.getType());
		db.insert(DBHelper.ENTRIES_TABLE_NAME, null, values);

		long returnValue = AccountManager.adjustBalance(db, transaction.getAccountId(), transaction.getAmount());		
		return returnValue;
	}
	
	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized long update(final SQLiteDatabase db, 
			final Transaction transaction, long oldAmount) {
		Integer payeeId = PayeeManager.getId(db, transaction.getPayee());
		if( payeeId == null ) {
			payeeId = PayeeManager.create(db, transaction.getPayee());
		}

		update(db, transaction);

		AccountManager.adjustBalance(db, transaction.getAccountId(), 0-oldAmount);
		long returnValue = AccountManager.adjustBalance(db, transaction.getAccountId(), transaction.getAmount());		

		return returnValue;
	}

	/**
	 * Create a new account with the given name
	 */
	
	public static synchronized void update(final SQLiteDatabase db, final Transaction transaction) {
		Integer payeeId = PayeeManager.getId(db, transaction.getPayee());
		if( payeeId == null ) {
			payeeId = PayeeManager.create(db, transaction.getPayee());
		}
		
		String[] whereArgs= { Integer.toString(transaction.getId()) };
		ContentValues values = new ContentValues();
		values.put("amount", transaction.getAmount());
		values.put("account_id", transaction.getAccountId());
		values.put("payee_id", payeeId);
		values.put("category_id", transaction.getCategoryId());
		values.put("timestamp", transaction.getTimestamp());
		values.put("type", transaction.getType());
		db.update(	DBHelper.ENTRIES_TABLE_NAME,
					values, 
					TransactionManager.GET_BY_ID_SQL, 
					whereArgs);
	}

	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static void delete(final SQLiteDatabase db, final Transaction transaction, boolean deleteLinked) {
		Integer linkedTransaction = transaction.getLinkId();
		if(linkedTransaction != null) {
			Transaction otherTransaction = TransactionManager.getById(db, linkedTransaction);
			if( deleteLinked ) {
				deleteFromDatabase(db, otherTransaction);
			} else {
				otherTransaction.setLinkId(null);
				update(db, otherTransaction);
			}
		}
		deleteFromDatabase(db, transaction);
	}
		

	/**
	 * Delete a transaction.
	 */
		
	private static void deleteFromDatabase(final SQLiteDatabase db, final Transaction transaction) {
		String[] whereArgs = { Integer.toString(transaction.getId()) };
		db.delete(DBHelper.ENTRIES_TABLE_NAME, TransactionManager.GET_BY_ID_SQL, whereArgs);		
		AccountManager.adjustBalance(db, transaction.getAccountId(), 0-transaction.getAmount());
	}

	/**
	 * Get the list of accounts from the database.
	 * 
	 * @param db database to query.
	 */
	
	public static void deleteAllForAccount(final SQLiteDatabase db, final Account account) {
		String[] whereArgs = { Integer.toString(account.id) };
		db.delete(DBHelper.ENTRIES_TABLE_NAME, TransactionManager.DELETE_FOR_ACCOUNT_SQL, whereArgs);
	}
}
