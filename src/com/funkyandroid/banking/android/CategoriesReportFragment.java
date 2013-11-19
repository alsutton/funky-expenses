package com.funkyandroid.banking.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.CategoryManager;
import com.funkyandroid.banking.android.data.CurrencyManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;
import com.funkyandroid.banking.android.utils.DatabaseRawQueryCursorLoader;

public class CategoriesReportFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>  {

	/**
	 * The query to get the categories for an account
	 */

	private static final String ACCOUNT_CATS_QUERY =
			"SELECT c._id as _id, c.name, sum(t.amount), count(*) FROM " +
				DBHelper.CATEGORIES_TABLE_NAME + " c, " +
				DBHelper.ENTRIES_TABLE_NAME + " t WHERE t.account_id = ? AND t.category_id = c._id GROUP BY t.category_id";

	/**
	 * The Adapter for the list
	 */

	private SpendingReportListAdapter mAdapter;

	/**
	 * The ID of the account being viewed.
	 */

	public Account account;

	/**
	 * The currency symbol
	 */

	public String currencySymbol;

	/**
	 * Long click handler for the categories list.
	 */
	private final AdapterView.OnItemLongClickListener longClickListener =
		new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> list, View view, int position, long id) {
				editCategory(((int)id & 0xffffffff));
				return true;
			}
		};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int accountId = getArguments().getInt("com.funkyandroid.banking.account_id", -1);
    	if( accountId == -1 ) {
    		getActivity().finish();
    		return;
    	}

    	final SQLiteDatabase database = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
    	account = AccountManager.getById(database, accountId);
    	if( account == null ) {
    		getActivity().finish();
    		return;
    	}

    	currencySymbol = CurrencyManager.getSymbol(database, account.currency);

    	mAdapter = new SpendingReportListAdapter();
    	setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Ensure the long click listener is set when we start
     */
    @Override
    public void onStart() {
    	super.onStart();

        getListView().setOnItemLongClickListener(longClickListener);
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
	 * Handle the clicking of a child object by taking the user
	 * to the list of apps in the category page.
	 *
	 * @see ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
    @Override
	public void onListItemClick(final ListView parent,
			final View view, final int position, final long id) {
		Intent intent = new Intent(view.getContext(), CategoryReportActivity.class);
		intent.putExtra("com.funkyandroid.banking.account_id", account.id);
		intent.putExtra("com.funkyandroid.banking.category_id", ((int)id&0xffffff));
		view.getContext().startActivity(intent);
		super.onListItemClick(parent, view, position, id);
	}

    /**
     * Handle a long press by showing the edit category dialog.
     *
     * @param categoryId The ID of the category to edit.
     */

    private void editCategory(final int categoryId) {
    	String name = CategoryManager.getById(
                    ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection(),
                    categoryId);

        final EditText categoryInput = new EditText(getActivity());
        if(name != null && name.length() > 0) {
            categoryInput.setText(name);
        }


    	new AlertDialog.Builder(getActivity())
                .setTitle(R.string.editCategory)
    			.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
                        final SQLiteDatabase writableDatabase = new DBHelper(getActivity()).getWritableDatabase();
                        try {
						    CategoryManager.updateCategory(writableDatabase, categoryId, categoryInput.getText().toString());
                            getLoaderManager().restartLoader(0, null, CategoriesReportFragment.this);
                        } finally {
                            writableDatabase.close();
                        }
					}
                })
    			.setNegativeButton(R.string.cancelButtonText, null)
    			.setView(categoryInput)
    			.show();
    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		final SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
		return new DatabaseRawQueryCursorLoader(getActivity(), db, ACCOUNT_CATS_QUERY, new String[] {Integer.toString(account.id)});
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
	public void onLoaderReset(Loader<Cursor> arg0) {
		 mAdapter.swapCursor(null);
	}

	/**
	 * The list adapter used for the expandable tree.
	 */

	public class SpendingReportListAdapter
		extends ResourceCursorAdapter {

		public SpendingReportListAdapter() {
			super(getActivity(), R.layout.title_value_list_item, null, 0);
		}

		@Override
		public void bindView(final View view, final Context context,
				final Cursor cursor) {
			((TextView)view.findViewById(R.id.name)).setText(cursor.getString(1));

			int balance = cursor.getInt(2);
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
			valueString.append(" in ");
			int entries = cursor.getInt(3);
			valueString.append(entries);
			if( entries == 1) {
				valueString.append(" entry.");
			} else {
				valueString.append(" entries.");
			}
			value.setText(valueString.toString());
		}
	}
}