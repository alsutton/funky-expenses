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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.CurrencyManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;
import com.funkyandroid.banking.android.utils.DatabaseRawQueryCursorLoader;

public class EntriesFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>  {

	/**
	 * The SQL to select the transactions for an account.
	 */

	private static final String TRANSACTIONS_FOR_ACCOUNT_SQL =
		"SELECT t._id as _id, p.name, t.amount, t.timestamp FROM "
		+ DBHelper.ENTRIES_TABLE_NAME
		+ " t, "
		+ DBHelper.PAYEE_TABLE_NAME
		+ " p WHERE t.account_id = ? AND p._id = t.payee_id ORDER BY timestamp DESC";


	/**
	 * The ID of the account being viewed.
	 */

	public Account account;

	/**
	 * The currency symbol
	 */

	public String currencySymbol;

	/**
	 * The list adapter
	 */

	private MyListAdapter mAdapter;

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

        mAdapter = new MyListAdapter();
		setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

	/**
     * Called whenever the activity becomes visible.
     */

    @Override
    public void onStart() {
    	super.onStart();
    	final SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
    	account = AccountManager.getById(db, account.id);
    }

	/**
	 * Restart the loader if we're resumed.
	 */

	@Override
	public void onResume() {
		super.onResume();
        getLoaderManager().restartLoader(0, null, this);
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
	 * Create the loader for the cursor.
	 *
	 * @param id The ID of the loader.
	 * @param args The arguments for the query.
	 *
	 * @return The loader.
	 */

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
		return new DatabaseRawQueryCursorLoader(getActivity(), db, TRANSACTIONS_FOR_ACCOUNT_SQL, new String[] { Integer.toString(account.id) } );
	}

    @Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
	public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
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
    	public MyListAdapter() {
    		super(getActivity(), R.layout.entry_list_item, null, 0);
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
