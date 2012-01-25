package com.funkyandroid.banking.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CategoryManager {

	public  static final String UNCAT_CAT = "Uncategorized";

	private static final String EMPTY_CATEGORY = "";

	private static final String[] SUGGEST_COLS = { "name", "_id" };

	private static final String SUGGEST_QUERY = "name like ?";

	private static final String[] ID_COL = { "_id" };

	private static final String ID_QUERY = "name = ?";

	public static final String[] NAME_COL = { "name" };

	private static final String NAME_QUERY = "_id = ?";

	private static final String ACCOUNT_CATS_QUERY =
		"SELECT c._id as _id, c.name, sum(t.amount), count(*) FROM " +
			DBHelper.CATEGORIES_TABLE_NAME + " c, " +
			DBHelper.ENTRIES_TABLE_NAME + " t WHERE t.account_id = ? AND t.category_id = c._id GROUP BY t.category_id";

	/**
	 * Get the id of a category from its' name.
	 *
	 * @param db database to query.
	 * @param name The name of the category to get
	 */

	public static int getByName(final SQLiteDatabase db, final String name) {
		return getByName( db, name, true );
	}

	/**
	 * Get the id of a category from its' name.
	 *
	 * @param db database to query.
	 * @param name The name of the category to get
	 */

	private static int getByName(final SQLiteDatabase db, final String name, boolean createIfNotPresent) {
		String[] whereValues = { name };
		Cursor cursor = db.query(
								DBHelper.CATEGORIES_TABLE_NAME,
								CategoryManager.ID_COL,
								CategoryManager.ID_QUERY,
								whereValues,
								null,
								null,
								null);

		try {
			if(cursor.moveToNext()) {
				if( cursor.isNull(0) ) {
					return Integer.MIN_VALUE;
				}
				return cursor.getInt(0);
			}

			if( createIfNotPresent ) {
				createCategory(db, name);
				return getByName(db, name, false);
			}

			return Integer.MIN_VALUE;
		} finally {
			cursor.close();
		}
	}

	/**
	 * Create a category entry for
	 * @param name
	 */
	private static void createCategory(final SQLiteDatabase db, final String name) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("name", name);
			db.insert(DBHelper.CATEGORIES_TABLE_NAME, null, values);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Create a category entry for
	 * @param name
	 */
	public static void updateCategory(final SQLiteDatabase db, final int id, final String name) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("name", name);

			final String[] whereArgs = { Integer.toString(id) };
			db.update(DBHelper.CATEGORIES_TABLE_NAME, values, "_id = ?", whereArgs);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Get a category name by the category ID.
	 *
	 * @param db The connection to the database
	 * @param categoryId The ID of the category to get.
	 * @return The name of the category
	 */
	public static String getById(final SQLiteDatabase db, final int categoryId) {
		if(categoryId == Integer.MIN_VALUE) {
			return EMPTY_CATEGORY;
		}

		String[] whereValues = { Integer.toString(categoryId) };
		Cursor cursor = db.query(
								DBHelper.CATEGORIES_TABLE_NAME,
								CategoryManager.NAME_COL,
								CategoryManager.NAME_QUERY,
								whereValues,
								null,
								null,
								null);

		try {
			if(cursor.moveToNext()) {
				if( cursor.isNull(0) ) {
					return EMPTY_CATEGORY;
				}
				return cursor.getString(0);
			}

			return EMPTY_CATEGORY;
		} finally {
			cursor.close();
		}
	}

	/**
	 * Get the matches to show in the autocompleter.
	 *
	 * @param db The database to get the information from.
	 * @param string What the user has typed so far.
	 *
	 * @return A cursor holding the list of possible options.
	 */
	public static Cursor getMatchesFor(final SQLiteDatabase db, final String string) {
		String[] whereArgs = { string+"%" };
		return db.query(DBHelper.CATEGORIES_TABLE_NAME, SUGGEST_COLS,
				SUGGEST_QUERY, whereArgs, null, null, "name ASC");
	}

	/**
	 * Get a cursor for the categories and totals in those categories used in
	 * an account.
	 *
	 * @param database The database being accessed.
	 * @param accountId The ID of the account to get the data for.
	 *
	 * @return A cursor pointing at the data.
	 */
	public static Cursor getForAccount(final SQLiteDatabase database, final int accountId) {
		String[] whereValues = { Integer.toString(accountId) };
		return database.rawQuery (CategoryManager.ACCOUNT_CATS_QUERY, whereValues);
	}
}
