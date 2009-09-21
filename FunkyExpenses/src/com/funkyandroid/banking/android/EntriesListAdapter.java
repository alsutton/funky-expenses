package com.funkyandroid.banking.android;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * The adapter showing the list of server statuses
 */
public class EntriesListAdapter 
	extends CursorAdapter {
	/**
	 * The currency code for all transactions.
	 */
	
	private String currencySymbol;
	
	/**
	 * Constructor. Passes all arguments to super class.
	 * 
	 * @param context The context in which the adapter is operating
	 * @param cursor The cursor being used.
	 */
	public EntriesListAdapter(final Context context, final Cursor c,
			final String currencySymbol) {
		super(context, c);
		this.currencySymbol = currencySymbol;
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		((EntryView)view).updateData(cursor, currencySymbol);
		
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup viewGroup) {
		return new EntryView(context, cursor, currencySymbol);
	}

	
}