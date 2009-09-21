package com.funkyandroid.banking.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * The adapter showing the list of server statuses
 */

public class AccountsListAdapter extends CursorAdapter 
	implements OnItemClickListener, OnItemLongClickListener {

	/**
	 * Constructor.
	 */
	public AccountsListAdapter(final Context context, final Cursor cursor) {
		super(context, cursor);
	}
	
	/**
	 * Bind the cursor entry to a view
	 */
	
	@Override
	public void bindView(final View view, final Context content, final Cursor cursor) {
		populateView( (AccountView) view, cursor);
	}
	
	/**
	 * Create a new view for the details from an item
	 */
	
	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		AccountView accountView = new AccountView(context);
		populateView(accountView, cursor);
		return accountView;
	}

	/**
	 * Populate an account view with the data from a cursor.
	 */
	
	private void populateView(final AccountView view, final Cursor cursor) {
		view.setNameText(cursor.getString(1));
		view.setBalance(cursor.getLong(3), cursor.getString(4));				
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