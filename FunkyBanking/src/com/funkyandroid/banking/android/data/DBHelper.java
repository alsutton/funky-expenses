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
		"  timestamp INT(8), category_id INT, payee_id INT, " +
		"  type INT, amount INT(8), notes TEXT);";
	
	/**
	 * The SQL to create the temporary table to migrate entries into
	 */

	private static final String ENTRIES_TEMP_TABLE_CREATE_SQL = 
		"CREATE TEMPORARY TABLE IF NOT EXISTS " + ENTRIES_TABLE_NAME + 
		"_temp (_id  integer primary key autoincrement, account_id INT, "+
		"  timestamp INT(8), category_id INT, payee_id INT, " +
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
		"CREATE TABLE IF NOT EXISTS " + RECURRING_TABLE_NAME + 
		" (_id INT, account_id INT, next_due INT(8), category_id INT,"+
		"  payee_id INT, type INT, amount INT(8), repaeat_count INT, repeat_unit INT);";	
	
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
		 super(context, "FunkyBanking", null, 9);
	 }
	 
	@Override
	public void onCreate(final SQLiteDatabase db) {
		DBHelper.createTables(db);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		if(oldVersion < 4) {
			db.execSQL(DBHelper.SETTINGS_TABLE_CREATE_SQL);
		}
		if(oldVersion < 6) {
			int catId = CategoryManager.getByName(db, CategoryManager.UNCAT_CAT);
			db.execSQL("UPDATE "+DBHelper.ENTRIES_TABLE_NAME+" SET category_id = "+catId+" WHERE category_id is null OR category_id = 0");
		}
		if(oldVersion < 7) {
			db.execSQL(DBHelper.ENTRIES_TEMP_TABLE_CREATE_SQL);
			db.execSQL("INSERT INTO "+
						ENTRIES_TABLE_NAME+
						"_temp SELECT _id, account_id, timestamp, category_id, payee_id, type, amount FROM "+
						ENTRIES_TABLE_NAME+";");
			db.execSQL("DROP TABLE "+ENTRIES_TABLE_NAME+";");
			db.execSQL(DBHelper.ENTRIES_TABLE_CREATE_SQL);
			db.execSQL("INSERT INTO "+
					ENTRIES_TABLE_NAME+
					" SELECT _id, account_id, timestamp, category_id, payee_id, type, amount FROM "+
					ENTRIES_TABLE_NAME+
					"_temp;");
			db.execSQL("DROP TABLE "+ENTRIES_TABLE_NAME+"_temp;");
			db.execSQL("ALTER TABLE "+DBHelper.ENTRIES_TABLE_NAME+" ADD link_id INT");
			
			db.execSQL("DROP TABLE IF EXISTS "+RECURRING_TABLE_NAME+"_temp;");
			db.execSQL(DBHelper.RECURRING_TABLE_CREATE_SQL);
			DBHelper.createIndexes(db);
		}
		if(oldVersion < 8) {
			db.execSQL("UPDATE "+DBHelper.ENTRIES_TABLE_NAME+" SET link_id = 0 WHERE link_id is null");			
		}
		if(oldVersion < 9) {
			db.execSQL("ALTER TABLE "+DBHelper.ENTRIES_TABLE_NAME+" ADD notes TEXT");		
		}
	}
	
	/**
	 * Create the database tables.
	 */
	
	public static void createTables(final SQLiteDatabase db) {
		db.beginTransaction();
		try {
			db.execSQL(DBHelper.ACCOUNTS_TABLE_CREATE_SQL);
			db.execSQL(DBHelper.CATEGORIES_TABLE_CREATE_SQL);
			db.execSQL(DBHelper.ENTRIES_TABLE_CREATE_SQL);
			db.execSQL("ALTER TABLE "+DBHelper.ENTRIES_TABLE_NAME+" ADD link_id INT");
			db.execSQL(DBHelper.PAYEE_TABLE_CREATE_SQL);
			db.execSQL(DBHelper.RECURRING_TABLE_CREATE_SQL);
			db.execSQL(DBHelper.SETTINGS_TABLE_CREATE_SQL);
			createIndexes(db);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	/**
	 * Create the indexes on the data.
	 */
	
	private static void createIndexes(final SQLiteDatabase db) {
		db.execSQL("CREATE INDEX IF NOT EXISTS en_dt ON "+ENTRIES_TABLE_NAME+"(timestamp)");
		db.execSQL("CREATE INDEX IF NOT EXISTS ac_name ON "+ACCOUNTS_TABLE_NAME+"(name)");
		db.execSQL("CREATE INDEX IF NOT EXISTS ca_name ON "+CATEGORIES_TABLE_NAME+"(name)");
		db.execSQL("CREATE INDEX IF NOT EXISTS pa_name ON "+PAYEE_TABLE_NAME+"(name)");
		db.execSQL("CREATE INDEX IF NOT EXISTS se_name ON "+SETTINGS_TABLE_NAME+"(name)");
	}

	/**
	 * Drop the database tables.
	 */
	
	public static void dropTables(final SQLiteDatabase db) {
		db.beginTransaction();
		try {
			db.execSQL("DROP TABLE IF EXISTS "+DBHelper.ACCOUNTS_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+DBHelper.CATEGORIES_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+DBHelper.ENTRIES_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+DBHelper.PAYEE_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+DBHelper.RECURRING_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "+DBHelper.SETTINGS_TABLE_NAME);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
}
