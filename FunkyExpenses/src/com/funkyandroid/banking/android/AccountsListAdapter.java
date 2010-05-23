package com.funkyandroid.banking.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;

/**
 * The adapter showing the list of server statuses
 */

public class AccountsListAdapter extends ResourceCursorAdapter 
	implements OnItemClickListener, OnItemLongClickListener {

	/**
	 * Constructor.
	 */
	public AccountsListAdapter(final Context context, final Cursor cursor) {
		super(context, R.layout.account_list_item, cursor);
	}
	
	/**
	 * Populate an account view with the data from a cursor.
	 */
	
	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		((TextView)view.findViewById(R.id.name)).setText(cursor.getString(1));

		final long balance = cursor.getLong(3);
		final TextView value = (TextView)view.findViewById(R.id.value);
		if			( balance < 0 ) {
			value.setTextColor(Color.rgb(0xc0, 0x00, 0x00));
		} else if	( balance > 0 ) {
			value.setTextColor(Color.rgb(0x00, 0xc0, 0x00));
		} else {
			value.setTextColor(Color.rgb(0xcf, 0xc0, 0x00));
		}
				
		final StringBuilder valueString = new StringBuilder(32);
		valueString.append("Balance : ");
		BalanceFormatter.format(valueString, balance, cursor.getString(4));
		valueString.append(' ');
		value.setText(valueString.toString());
	}
	
	/**
	 * Handle clicks by opening a browser window for the app.
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Context context = parent.getContext();
		Intent viewIntent = new Intent(context, EntriesActivity.class);
		viewIntent.putExtra("com.funkyandroid.banking.account_id", ((int)id & 0xffff));
		context.startActivity(viewIntent);    				
	}

	/**
	 * A long click takes the user to the edit page.
	 */
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {
		Context context = parent.getContext();
		Intent viewIntent = new Intent(context, EditAccountActivity.class);
		viewIntent.putExtra("com.funkyandroid.banking.account_id", ((int)id & 0xffff));
		context.startActivity(viewIntent);    				
		return true;
	}
}