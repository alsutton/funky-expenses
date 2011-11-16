package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.CurrencyManager;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;

public class EntriesFragment extends ListFragment {

	/**
	 * The ID of the account being viewed.
	 */

	public Account account;

	/**
	 * The currency symbol
	 */

	public String currencySymbol;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	int accountId = getArguments().getInt("com.funkyandroid.banking.account_id", -1);
    	if( accountId == -1 ) {
    		getActivity().finish();
    		return;
    	}

    	final SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
    	account = AccountManager.getById(db, accountId);
    	if( account == null ) {
    		getActivity().finish();
    		return;
    	}

		currencySymbol = CurrencyManager.getSymbol(db, account.currency);

        final Cursor entryCursor = TransactionManager.getForAccount(db, accountId);
    	getActivity().startManagingCursor(entryCursor);
    	setListAdapter(new MyListAdapter(entryCursor));
    }

    /**
     * Creates the view for this fragment
     */

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.entries, container, false);
    }

	/**
     * Called whenever the activity becomes visible.
     */

    @Override
    public void onStart() {
    	super.onStart();
    	final SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
    	account = AccountManager.getById(db, account.id);
    	updateBalance(account.balance);
		((MyListAdapter)getListAdapter()).notifyDataSetChanged();
    }

	/**
	 * Handle clicks by opening a the entry editor
	 */
    @Override
	public void onListItemClick(final ListView list, final View view, int position, long id) {
		Intent intent = new Intent(getActivity(), EditEntryActivity.class);
		intent.putExtra("com.funkyandroid.banking.transaction_id", ((int)id&0xffffff));
		intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
		startActivity(intent);
	}

    /**
     * Update the current account balance
     */

    public void updateBalance(long newBalance) {
    	StringBuilder balanceText = new StringBuilder(32);
    	balanceText.append("Current balance : ");
		BalanceFormatter.format(balanceText, newBalance, currencySymbol);

    	TextView textView = (TextView) getView().findViewById(R.id.balance);
    	textView.setText(balanceText.toString());
    }

    /**
     * The adapter showing the list of server statuses
     */
    public final class MyListAdapter
    	extends ResourceCursorAdapter {

    	/**
    	 * The date formatter
    	 */

    	private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

    	/**
    	 * @param context The context in which the adapter is operating
    	 * @param cursor The cursor being used.
    	 */
    	public MyListAdapter(final Cursor c) {
    		super(getActivity(), R.layout.entry_list_item, c);
    	}

    	/**
    	 * Populate an entry view with the data from a cursor.
    	 */

    	@Override
    	public void bindView(final View view, final Context context, final Cursor cursor) {
    		((TextView)view.findViewById(R.id.name)).setText(cursor.getString(1));

    		final long balance = cursor.getLong(2);
    		View sideBar = view.findViewById(R.id.sidebar);
    		if			( balance < 0 ) {
    			sideBar.setBackgroundColor(Color.rgb(0xc0, 0x00, 0x00));
    		} else if	( balance > 0 ) {
    			sideBar.setBackgroundColor(Color.rgb(0x00, 0xc0, 0x00));
    		} else {
    			sideBar.setBackgroundColor(Color.rgb(0xc0, 0xc0, 0xc0));
    		}

    		final TextView value = (TextView)view.findViewById(R.id.value);
    		StringBuilder valueString = new StringBuilder(10);
    		BalanceFormatter.format(valueString, balance, currencySymbol);
    		value.setText(valueString.toString());

    		Date entryDate = new Date(cursor.getLong(3));
    		((TextView)view.findViewById(R.id.date)).setText(sdf.format(entryDate));
    	}
    }
}
