package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * The adapter showing the list of server statuses
 */
public class EntriesListAdapter 
	extends ResourceCursorAdapter {
	/**
	 * The currency code for all transactions.
	 */
	
	private String currencySymbol;
	
	/**
	 * @param context The context in which the adapter is operating
	 * @param cursor The cursor being used.
	 */
	public EntriesListAdapter(final Context context, final Cursor c,
			final String currencySymbol) {
		super(context, R.layout.entry_list_item, c);
		this.currencySymbol = currencySymbol;
	}

	/**
	 * Populate an entry view with the data from a cursor.
	 */
	
	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		((TextView)view.findViewById(R.id.name)).setText(cursor.getString(1));

		final TextView value = (TextView)view.findViewById(R.id.value);
		final long balance = cursor.getLong(2);
		if			( balance < 0 ) {
			value.setTextColor(Color.rgb(0xc0, 0x00, 0x00));
		} else if	( balance > 0 ) {
			value.setTextColor(Color.rgb(0x00, 0xc0, 0x00));
		} else {
			value.setTextColor(Color.rgb(0xcf, 0xc0, 0x00));
		}
		
		StringBuilder valueString = new StringBuilder(10);
		BalanceFormatter.format(valueString, balance, currencySymbol);
		value.setText(valueString.toString());

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
		Date entryDate = new Date(cursor.getLong(3));
		((TextView)view.findViewById(R.id.date)).setText(sdf.format(entryDate));
	}
}