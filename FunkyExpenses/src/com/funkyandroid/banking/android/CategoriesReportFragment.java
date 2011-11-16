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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.CategoryManager;
import com.funkyandroid.banking.android.data.CurrencyManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;

public class CategoriesReportFragment extends ListFragment {

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
				return false;
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
    }

    /**
     * Called whenever the activity becomes visible.
     */

    @Override
    public void onStart() {
    	super.onStart();
    	updateBalance(AccountManager.getBalanceById(((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection(), account.id));
    	((ResourceCursorAdapter)getListAdapter()).notifyDataSetChanged();
    }

    /**
     * Creates the view for this fragment
     */

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	final View view = inflater.inflate(R.layout.categories, container, false);

    	final SQLiteDatabase database = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
        Cursor categoryCursor = CategoryManager.getForAccount(database, account.id);
    	getActivity().startManagingCursor(categoryCursor);

    	setListAdapter(new SpendingReportListAdapter(categoryCursor));
    	ListView listView = (ListView)view.findViewById(android.R.id.list);
    	listView.setOnItemLongClickListener(longClickListener);

    	return view;
    }

	/**
	 * Handle the clicking of a child object by taking the user
	 * to the list of apps in the category page.
	 *
	 * @param parent The list view clicked on.
	 * @param view The view clicked upon.
	 * @param groupPosition The position of the group in the ExpandableListView
	 * @param childPosition The position of the child in the group
	 * @param id The ID of the item clicked on.
	 *
	 * @return Always true to indicate activity was started.
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
     * Handle a long press by showing the edit category dialog.
     *
     * @param categoryId The ID of the category to edit.
     */

    private void editCategory(final int categoryId) {
    	final SQLiteDatabase database = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
    	String name = CategoryManager.getById(database, categoryId);

    	LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE);
    	final View editView = inflater.inflate(R.layout.edit_category, null);
    	if(name != null && name.length() > 0) {
    		((EditText)(editView.findViewById(R.id.categoryName))).setText(name);
    	}

    	new AlertDialog.Builder(getActivity())
    			.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText catName = (EditText) editView.findViewById(R.id.categoryName);
						if(catName == null || catName.getText().length() == 0) {
							return;
						}
						CategoryManager.updateCategory(database, categoryId, catName.getText().toString());
						((ResourceCursorAdapter)CategoriesReportFragment.this.getListAdapter()).getCursor().requery();
					}
    			})
    			.setNegativeButton(R.string.cancelButtonText, null)
    			.setView(editView)
    			.setTitle(R.string.editCategory)
    			.create()
    			.show();

    }

	/**
	 * The list adapter used for the expandable tree.
	 */

	public class SpendingReportListAdapter
		extends ResourceCursorAdapter {

		public SpendingReportListAdapter(final Cursor cursor) {
			super(CategoriesReportFragment.this.getActivity(), R.layout.category_list_item, cursor);
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