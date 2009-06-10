package com.funkyandroid.banking.android.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper 
	extends SQLiteOpenHelper {

	/**
	 * The accounts table name.
	 */
	
	public static final String ACCOUNTS_TABLE_NAME = "accounts";
	
	/**
	 * The SQL to create the update table.
	 */
	
	private static final String ACCOUNTS_TABLE_CREATE_SQL = 
		"CREATE TABLE IF NOT EXISTS " + ACCOUNTS_TABLE_NAME +
		" (_id  integer primary key autoincrement, opening_balance INT(8), " +
		"	balance INT(8), name TEXT, currency TEXT);";

	/**
	 * The categories table name.
	 */
	
	public static final String CATEGORIES_TABLE_NAME = "categories";
	
	/**
	 * The SQL to create the categories table
	 */
	
	private static final String CATEGORIES_TABLE_CREATE_SQL = 
		"CREATE TABLE IF NOT EXISTS " + CATEGORIES_TABLE_NAME + 
		"(_id  integer primary key autoincrement, name TEXT);";
	
	/**
	 * The chart table name.
	 */
	
	public static final String ENTRIES_TABLE_NAME = "entries";
	
	/**
	 * The SQL to create the chart table.
	 */

	private static final String ENTRIES_TABLE_CREATE_SQL = 
		"CREATE TABLE IF NOT EXISTS " + ENTRIES_TABLE_NAME + 
		" (_id  integer primary key autoincrement, account_id INT, "+
		"  timestamp INT(8), category_id INT, payee_id INT, recipient_account_id INT, " +
		"  type INT, amount INT(8));";

	/**
	 * The payee table name.
	 */
	
	public static final String PAYEE_TABLE_NAME = "payees";
	
	/**
	 * The SQL to create the payees table
	 */
	
	private static final String PAYEE_TABLE_CREATE_SQL = 
		"CREATE TABLE IF NOT EXISTS " + PAYEE_TABLE_NAME + 
		"(_id  integer primary key autoincrement, name TEXT);";
	
	/**
	 * The recurring transactions table name.
	 */
	
	public static final String RECURRING_TABLE_NAME = "recurring";
	
	/**
	 * The SQL to create the chart table.
	 */

	private static final String RECURRING_TABLE_CREATE_SQL = 
		"CREATE TABLE IF NOT EXISTS " + ENTRIES_TABLE_NAME + 
		" (account_id INT, next_due INT(8), category_id INT, payee_id INT, type INT, amount INT(8), repaeat_count INT, repeat_unit INT);";	
	
	/**
	 * The recurring transactions table name.
	 */
	
	public static final String SETTINGS_TABLE_NAME = "settings";
	
	/**
	 * The SQL to create the chart table.
	 */

	private static final String SETTINGS_TABLE_CREATE_SQL = 
		"CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE_NAME + " (name TEXT, VALUE TEXT);";	
	
	 /**
	  * Constructor.
	  */
	
	 public DBHelper(final Context context) {
		 super(context, "FunkyBanking", null, 6);
	 }
	 
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DBHelper.ACCOUNTS_TABLE_CREATE_SQL);
		db.execSQL(DBHelper.CATEGORIES_TABLE_CREATE_SQL);
		db.execSQL(DBHelper.ENTRIES_TABLE_CREATE_SQL);
		db.execSQL(DBHelper.PAYEE_TABLE_CREATE_SQL);
		db.execSQL(DBHelper.RECURRING_TABLE_CREATE_SQL);
		db.execSQL(DBHelper.SETTINGS_TABLE_CREATE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion < 2) {
			db.execSQL("ALTER TABLE "+DBHelper.ENTRIES_TABLE_NAME+" ADD recipient_account_id INT");
			db.execSQL(DBHelper.SETTINGS_TABLE_CREATE_SQL);
		}
		if(oldVersion < 4) {
			db.execSQL("ALTER TABLE "+DBHelper.ENTRIES_TABLE_NAME+" ADD link_id INT");
			db.execSQL(DBHelper.SETTINGS_TABLE_CREATE_SQL);
		}
		if(oldVersion < 6) {
			int catId = CategoryManager.getByName(db, CategoryManager.UNCAT_CAT);
			db.execSQL("UPDATE "+DBHelper.ENTRIES_TABLE_NAME+" SET category_id = "+catId+" WHERE category_id is null OR category_id = 0");
		}
	}
}
