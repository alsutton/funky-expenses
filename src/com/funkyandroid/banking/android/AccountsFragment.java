package com.funkyandroid.banking.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;

public class AccountsFragment extends ListFragment
	implements OnItemLongClickListener {

	/**
	 * Constructor. Requires a connection to the database.
	 */

	@Override
	public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        final SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
        final Cursor accounts = AccountManager.getAll(db);
        getActivity().startManagingCursor(accounts);
        final MyListAdapter adapter = new MyListAdapter(accounts);

		setListAdapter(adapter);
		getListView().setOnItemLongClickListener(this);

		final SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		if(!prefs.getBoolean("hidetips", false)) {
	        if( adapter.isEmpty() ) {
	        	Toast.makeText(getActivity(), "Press the + to add an account", Toast.LENGTH_LONG).show();
	        } else {
	        	Toast.makeText(getActivity(), "Tap an account to view or add entries.\nPress and hold to edit account details.", Toast.LENGTH_LONG).show();
	        }
		}
	}

	/**
	 * On a resume refresh the list.
	 */

	@Override
	public void onResume() {
		super.onResume();
		((MyListAdapter)getListAdapter()).notifyDataSetChanged();
	}

	/**
	 * Handle clicks by opening a browser window for the app.
	 */
    @Override
	public void onListItemClick(final ListView list, final View view, final int position, final long id) {
		Intent viewIntent = new Intent(getActivity(), EntriesActivity.class);
		viewIntent.putExtra("com.funkyandroid.banking.account_id", ((int)id & 0xffff));
		startActivity(viewIntent);
	}

	/**
	 * A long click takes the user to the edit page.
	 */
	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position,
			final long id) {
		Intent viewIntent = new Intent(getActivity(), EditAccountActivity.class);
		viewIntent.putExtra("com.funkyandroid.banking.account_id", ((int)id & 0xffff));
		startActivity(viewIntent);
		return true;
	}


    /**
     * The adapter showing the list of server statuses
     */

    public final class MyListAdapter
    	extends ResourceCursorAdapter {

    	/**
    	 * Constructor.
    	 */
    	public MyListAdapter(final Cursor cursor) {
    		super(getActivity(), R.layout.account_list_item, cursor);
    	}

    	/**
    	 * Populate an account view with the data from a cursor.
    	 */

    	@Override
    	public void bindView(final View view, final Context context, final Cursor cursor) {
    		((TextView)view.findViewById(R.id.name)).setText(cursor.getString(1));

    		final long balance = cursor.getLong(3);
    		View sideBar = view.findViewById(R.id.sidebar);
    		if			( balance < 0 ) {
    			sideBar.setBackgroundColor(Color.rgb(0xc0, 0x00, 0x00));
    		} else if	( balance > 0 ) {
    			sideBar.setBackgroundColor(Color.rgb(0x00, 0xc0, 0x00));
    		} else {
    			sideBar.setBackgroundColor(Color.rgb(0xc0, 0xc0, 0xc0));
    		}

    		final TextView value = (TextView)view.findViewById(R.id.value);
    		final StringBuilder valueString = new StringBuilder(32);
    		valueString.append("Balance : ");
    		BalanceFormatter.format(valueString, balance, cursor.getString(4));
    		valueString.append(' ');
    		value.setText(valueString.toString());
    	}
    }

}
