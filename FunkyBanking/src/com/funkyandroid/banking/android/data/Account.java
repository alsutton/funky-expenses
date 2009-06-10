package com.funkyandroid.banking.android.data;

import android.database.Cursor;

public class Account {

	/**
	 * The account ID
	 */
	
	public int id;
	
	/**
	 * The name of the account.
	 */
	
	public String name;
	
	/**
	 * The currency for the account.
	 */
	
	public String currency;
	
	/**
	 * The value. This is multiplied by 100
	 */
	
	public long openingBalance;
	
	/**
	 * The value. This is multiplied by 100
	 */
	
	public long balance;

	/**
	 * Null constructor.
	 */
	
	public Account() {
		super();
		openingBalance = balance = 0;
	}
	
	/**
	 * Construct the object from a cursor.
	 * 
	 */
	
	public Account(final Cursor cursor) {
		id = cursor.getInt(0);
		name = cursor.getString(1);
		openingBalance = cursor.getLong(2);
		balance = cursor.getLong(3);
		currency = cursor.getString(4);
	}
}
