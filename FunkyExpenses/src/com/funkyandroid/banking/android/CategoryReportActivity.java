package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.CurrencyManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.adfree.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class CategoryReportActivity extends ListActivity {

	/**
	 * The ID of the account being viewed.
	 */

	private int accountId;

	/**
	 * The category the report is for
	 */

	private int categoryId;

	/**
	 * The connection to the database.
	 */

	private SQLiteDatabase database;

	/**
	 * The currency symbol
	 */

	private String currencySymbol;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.categories);

    	Intent startingIntent = getIntent();
    	accountId = startingIntent.getIntExtra("com.funkyandroid.banking.account_id", -1);
    	if( accountId == -1 ) {
    		finish();
    		return;
    	}

    	categoryId = startingIntent.getIntExtra("com.funkyandroid.banking.category_id", -1);
    	if( categoryId == -1 ) {
    		finish();
    		return;
    	}

    	database = (new DBHelper(this)).getReadableDatabase();
    	Account account = AccountManager.getById(database, accountId);
    	if( account == null ) {
    		database.close();
    		finish();
    		return;
    	}

    	currencySymbol = CurrencyManager.getSymbol(database, account.currency);

		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(account.name);

		Cursor cursor = TransactionManager.getCursorForCategoryAndAccount(database, accountId, categoryId);
		startManagingCursor(cursor);
    	setListAdapter( new MyAdapter(cursor) );


		Button button = (Button) findViewById(R.id.entries_button);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					startEntries();
        				}
        		});
    }

    /**
     * Called whenever the activity becomes visible.
     */

    @Override
    public void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, "8SVYESRG63PTLMNLZPPU");
    	updateBalance(AccountManager.getBalanceById(database, accountId));
		((ResourceCursorAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }

    /**
     * Close database connection onDestroy.
     */

    @Override
	public void onDestroy() {
    	if(database != null && database.isOpen()) {
    		database.close();
    	}
    	super.onDestroy();
    }

    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(R.string.menuEntries)
			.setIcon(android.R.drawable.ic_menu_revert)
			.setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(final MenuItem item) {
						startEntries();
			            return true;
					}
				}
			);

		MenuUtil.buildMenu(this, menu);

		return true;
	}

    /**
     * Start the entries activity
     */

    private void startEntries() {
		Intent intent = new Intent(this, EntriesActivity.class);
		intent.putExtra("com.funkyandroid.banking.account_id", accountId);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
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
	public void onListItemClick(final ListView parent, final View view, final int position, final long id) {
		Intent intent = new Intent(view.getContext(), EditEntryActivity.class);
		intent.putExtra("com.funkyandroid.banking.transaction_id", ((int)id&0xffffff));
		intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
		view.getContext().startActivity(intent);
	}


	/**
     * Update the current account balance
     */

    public void updateBalance(long newBalance) {
    	StringBuilder balanceText = new StringBuilder(32);
    	balanceText.append("Current balance : ");
		BalanceFormatter.format(balanceText, newBalance, currencySymbol);

    	TextView textView = (TextView) findViewById(R.id.balance);
    	textView.setText(balanceText.toString());
    }

	/**
	 * The list adapter used for the expandable tree.
	 */

	public class MyAdapter
		extends ResourceCursorAdapter {

		public MyAdapter(final Cursor cursor) {
			super(CategoryReportActivity.this, R.layout.entry_list_item, cursor );
		}

		@Override
		public void bindView(final View view, final Context context, final Cursor cursor) {
			Log.i("FE", "BIndin");
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