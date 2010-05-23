package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.flurry.android.FlurryAgent;
import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.CategoryManager;
import com.funkyandroid.banking.android.data.CurrencyManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class SpendingReportActivity extends Activity {
	
	/**
	 * The adapter holding the entry information.
	 */
	
	private SpendingReportListAdapter adapter;
	
	/**
	 * The ID of the account being viewed.
	 */
	
	private int accountId;

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
        setContentView(R.layout.spending);
    	
    	Intent startingIntent = getIntent();    	
    	accountId = startingIntent.getIntExtra("com.funkyandroid.banking.account_id", -1);
    	if( accountId == -1 ) {
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

        Cursor categoryCursor = CategoryManager.getForAccount(database, accountId);
    	startManagingCursor(categoryCursor);
    	
    	adapter = new SpendingReportListAdapter(categoryCursor); 
    	ExpandableListView list = (ExpandableListView) findViewById(R.id.informationList);
    	list.setAdapter(adapter);
    	list.setOnChildClickListener(adapter);

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
		adapter.notifyDataSetChanged();
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
		startActivity(intent);
		finish();    	
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
	
	public class SpendingReportListAdapter 
		extends ResourceCursorTreeAdapter 
		implements OnChildClickListener {

		public SpendingReportListAdapter(final Cursor cursor) {
			super(SpendingReportActivity.this, cursor, R.layout.category_list_item, R.layout.entry_list_item);
		}

		@Override
		public void bindGroupView(final View view, final Context context,
				final Cursor cursor, final boolean isExpanded) {
			((TextView)view.findViewById(R.id.name)).setText(cursor.getString(1));

			final TextView value = (TextView)view.findViewById(R.id.value);
			int balance = cursor.getInt(2);
			if			( balance < 0 ) {
				value.setTextColor(Color.rgb(0xc0, 0x00, 0x00));
			} else if	( balance > 0 ) {
				value.setTextColor(Color.rgb(0x00, 0xc0, 0x00));
			} else {
				value.setTextColor(Color.rgb(0xcf, 0xc0, 0x00));
			}			
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

		@Override
		public void bindChildView(final View view, final Context context,
				final Cursor cursor, final boolean isLastChild) {
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

		@Override
		protected Cursor getChildrenCursor(final Cursor groupCursor) {
			Cursor cursor = TransactionManager.getCursorForCategoryAndAccount(database, accountId, groupCursor.getInt(0));
			SpendingReportActivity.this.startManagingCursor(cursor);
			return cursor;
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
		public boolean onChildClick(final ExpandableListView parent,
				final View view, final int groupPosition,
				final int childPosition, final long childId) {
			Intent intent = new Intent(view.getContext(), EditEntryActivity.class);
			intent.putExtra("com.funkyandroid.banking.transaction_id", ((int)childId&0xffffff));
			intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
			view.getContext().startActivity(intent);    				
			return true;
		}
	}
}