package com.funkyandroid.banking.android.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CurrencyManager {

	/**
	 * Used where the symbol is unknown
	 */
	
	private static final String UNKNOWN_SYMBOL = "";
	
	private static final String[] GET_SYMBOL_COL = { "symbol" };
	
	private static final String GET_SYMBOL_QUERY = "short_code = ?";

	private static final String[] GET_SHORTCODES_COL = { "short_code" };

	/**
	 * Get the id of a category from its' name.
	 * 
	 * @param db database to query.
	 * @param name The name of the category to get
	 */
	
	public static String getSymbol(final SQLiteDatabase db, final String shortCode) {
		String[] whereValues = { shortCode };
		Cursor cursor = db.query(
								DBHelper.CURRENCIES_TABLE_NAME, 
								CurrencyManager.GET_SYMBOL_COL, 
								CurrencyManager.GET_SYMBOL_QUERY, 
								whereValues, 
								null, 
								null, 
								null);
		
		try {
			if(!cursor.moveToNext()) {
				return UNKNOWN_SYMBOL;
			}
			
			if( cursor.isNull(0) ) {
				return UNKNOWN_SYMBOL;
			}

			return cursor.getString(0);
		} finally {
			cursor.close();
		}
	}

	/**
	 * Create a currency entry.
	 * @param name
	 */
	
	public static void create(final SQLiteDatabase database, String code, String symbol) {
		ContentValues values = new ContentValues();
		values.put("short_code", code);
		values.put("symbol", symbol);
		database.insert(DBHelper.CURRENCIES_TABLE_NAME, null, values);
	}

	/**
	 * Get all the available currency shortcodes.
	 * 
	 * @param database The connection to the database.
	 */
	public static List<String> getAllShortCodes(SQLiteDatabase db) {
		List<String> codes = new ArrayList<String>();
		
		Cursor cursor = db.query(
								DBHelper.CURRENCIES_TABLE_NAME, 
								CurrencyManager.GET_SHORTCODES_COL, 
								null, 
								null, 
								null, 
								null, 
								"short_code ASC");
		
		try {
			while(cursor.moveToNext()) {
				codes.add(cursor.getString(0));
			}
		} finally {
			cursor.close();
		}

		return codes;
	}
}
