package com.funkyandroid.banking.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import com.funkyandroid.banking.android.data.*;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;
import com.funkyandroid.banking.android.utils.DatabaseRawQueryCursorLoader;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CategoryReportFragment
    extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * The SQL to select the transactions for an account.
     */

    private static final String ACCOUNT_AND_CATEGORY_QUERY =
            "SELECT t._id as _id, p.name, t.amount, t.timestamp FROM "
                    + DBHelper.ENTRIES_TABLE_NAME
                    + " t, "
                    + DBHelper.PAYEE_TABLE_NAME
                    + " p WHERE t.account_id = ? AND t.category_id = ? AND p._id = t.payee_id ORDER BY timestamp DESC";

    /**
	 * The ID of the account being viewed.
	 */

	private int accountId;

    /**
     * The ID of the category being viewed.
     */

    private int categoryId;

	/**
	 * The currency symbol
	 */

	private String currencySymbol;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.categories, container, false);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
    	accountId = arguments.getInt("com.funkyandroid.banking.account_id", -1);
    	if( accountId == -1 ) {
    		getActivity().finish();
    		return;
    	}

    	categoryId = arguments.getInt("com.funkyandroid.banking.category_id", -1);
    	if( categoryId == -1 ) {
    		getActivity().finish();
    		return;
    	}

        SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
    	Account account = AccountManager.getById(db, accountId);
    	if( account == null ) {
    		getActivity().finish();
    		return;
    	}

    	currencySymbol = CurrencyManager.getSymbol(db, account.currency);
        getActivity().getActionBar().setTitle(CategoryManager.getById(db, categoryId));

        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Restart the loader on a resume to ensure the data is fresh.
     */

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

	/**
	 * Handle the clicking of a child object by taking the user
	 * to the list of apps in the category page.
	 *
	 * @param parent The list view clicked on.
	 * @param view The view clicked upon.
     * @param position The position of the selected item.
	 * @param id The ID of the item clicked on.
	 */

    @Override
	public void onListItemClick(final ListView parent, final View view, final int position, final long id) {
		Intent intent = new Intent(view.getContext(), EditEntryActivity.class);
		intent.putExtra("com.funkyandroid.banking.transaction_id", ((int)id&0xffffff));
		intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
		view.getContext().startActivity(intent);
	}


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
        return new DatabaseRawQueryCursorLoader(
                getActivity(),
                db,
                ACCOUNT_AND_CATEGORY_QUERY,
                new String[] { Integer.toString(accountId), Integer.toString(categoryId) } );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MyAdapter adapter = new MyAdapter(data);
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setListAdapter(null);
    }

	/**
	 * The list adapter used for the expandable tree.
	 */

	public class MyAdapter
		extends ResourceCursorAdapter {

		public MyAdapter(final Cursor cursor) {
			super(getActivity(), R.layout.entry_list_item, cursor, 0 );
		}

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

			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
			Date entryDate = new Date(cursor.getLong(3));
			((TextView)view.findViewById(R.id.date)).setText(sdf.format(entryDate));
		}
	}
}