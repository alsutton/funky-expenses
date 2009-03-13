package com.funkyandroid.banking.android.data;

import android.database.Cursor;

public class Account {

	/**
	 * The account ID
	 */
	
	private int id;
	
	/**
	 * The name of the account.
	 */
	
	private String name;
	
	/**
	 * The currency for the account.
	 */
	
	private String currency;
	
	/**
	 * The value. This is multiplied by 100
	 */
	
	private long openingBalance;
	
	/**
	 * The value. This is multiplied by 100
	 */
	
	private long balance;

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
	
	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(final long balance) {
		this.balance = balance;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public long getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(long openingBalance) {
		this.openingBalance = openingBalance;
	}
}
