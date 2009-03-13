package com.funkyandroid.banking.android.data;

import android.database.Cursor;

public class Transaction {

	/**
	 * The transaction types.
	 */
	
	public static final int TYPE_TRANSFER_OUT = -2,
							TYPE_DEBIT = -1,
							TYPE_CREDIT = 1,
							TYPE_TRANSFER_IN = 2;
	
	/**
	 * The transaction ID
	 */
	
	private int id;
	
	/**
	 * The account ID to which the transaction belongs
	 */
	
	private int accountId;
	
	/**
	 * The timestamp for the transaction.
	 */
	
	private long timestamp;
	
	/**
	 * The category ID for the entry
	 */
	
	private int categoryId;
	
	/**
	 * The category for the entry
	 */
	
	private String category;
	
	/**
	 * The id of the payee.
	 */
	
	private int payeeId;
	
	/**
	 * The currency for the account.
	 */
	
	private String payee;
	
	/**
	 * The value. This is multiplied by 100
	 */
	
	private int type;

	/**
	 * The amount the transaction is for
	 */
	
	private long amount;
		
	/**
	 * Create a transaction with a specified account id.
	 */
	
	public Transaction() {
		super();
		this.timestamp = System.currentTimeMillis();
	}
	
	/**
	 * Create transaction from a cursor.
	 */
	
	public Transaction(final Cursor cursor) {
		this.id = cursor.getInt(0);
		this.accountId = cursor.getInt(1);
		this.timestamp = cursor.getLong(2);
		this.categoryId = cursor.getInt(3);
		this.payeeId = cursor.getInt(4);
		this.type = cursor.getInt(5);
		this.amount = cursor.getLong(6);
	}
	
	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(final int accountId) {
		this.accountId = accountId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getPayeeId() {
		return payeeId;
	}
	
	public void setPayeeId(final int payeeId) {
		this.payeeId = payeeId;
	}
	
	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}
}
