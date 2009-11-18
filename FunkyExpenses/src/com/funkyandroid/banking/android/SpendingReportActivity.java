package com.funkyandroid.banking.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;

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
    }

    /**
     * Called whenever the activity becomes visible.
     */
    
    @Override
    public void onStart() {
    	super.onStart();
    	updateBalance(AccountManager.getBalanceById(database, accountId));
		adapter.notifyDataSetChanged();
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
						Context context = SpendingReportActivity.this;
						Intent intent = new Intent(context, EntriesActivity.class);
						intent.putExtra("com.funkyandroid.banking.account_id", accountId);
						context.startActivity(intent);
						finish();
			            return true;						
					}
				}
			);
        
		MenuUtil.buildMenu(this, menu);
		
		return true;
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
		extends CursorTreeAdapter 
		implements OnChildClickListener {

		public SpendingReportListAdapter(final Cursor cursor) {
			super(cursor, SpendingReportActivity.this);
		}

		@Override
		public void bindGroupView(final View view, final Context context,
				final Cursor cursor, final boolean isExpanded) {
			final CategoryView row = (CategoryView) view;
			row.setCategory(cursor);
		}

		@Override
		public void bindChildView(final View view, final Context context,
				final Cursor cursor, final boolean isLastChild) {
			final EntryView row = (EntryView) view;
			row.updateData(cursor, currencySymbol);
		}

		@Override
		protected Cursor getChildrenCursor(final Cursor groupCursor) {
			Cursor cursor = TransactionManager.getCursorForCategoryAndAccount(database, accountId, groupCursor.getInt(0));
			SpendingReportActivity.this.startManagingCursor(cursor);
			return cursor;
		}

		@Override
		protected View newChildView(final Context context, final Cursor cursor,
				final boolean isLastChild, final ViewGroup parent) {			
			final EntryView view = new EntryView(parent.getContext(), cursor, currencySymbol); 
			bindChildView(view,context,cursor,isLastChild);
			return view;
		}

		@Override
		protected View newGroupView(final Context context, final Cursor cursor,
				final boolean isExpanded, final ViewGroup parent) {
			final CategoryView view = new CategoryView(parent.getContext()); 
			bindGroupView(view,context,cursor,isExpanded);
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


	/**
	 * The view to show a category.
	 */
	
	public class CategoryView extends LinearLayout {

		/**
		 * The view holding the name of the category
		 */
		
		private final transient TextView category;
		
		/**
		 * The view holding the number of entries in that category.
		 */
		
		private final transient TextView amount;

		/**
		 * Constructor. Builds the views.
		 * 
		 * @param context The context in which the app is operating.
		 */
		public CategoryView(final Context context) {
			super(context);
		
			setOrientation(LinearLayout.VERTICAL);

			final ViewGroup.LayoutParams categoryLayout = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			category = new TextView(context);
			category.setLayoutParams(categoryLayout);
			category.setGravity(Gravity.CENTER_VERTICAL);
			category.setTextSize(category.getTextSize()+4);
			addView(category);

			final ViewGroup.LayoutParams amountLayout = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			amount = new TextView(context);
			amount.setLayoutParams(amountLayout);
			amount.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
			amount.setPadding(amount.getPaddingLeft(), amount.getPaddingTop(), 10, amount.getPaddingBottom());
			amount.setTypeface(amount.getTypeface(), Typeface.BOLD);
			addView(amount);

			setPadding(	40, getPaddingTop(), getPaddingRight(), getPaddingBottom());
		}
		
		/**
		 * Set the category details.
		 * 
		 * @cursor The cursor holding the category details.
		 */
		
		public void setCategory(final Cursor cursor) {					
			category.setText(cursor.getString(1));

			int balance = cursor.getInt(2);
			if			( balance < 0 ) {
				amount.setTextColor(Color.rgb(0xc0, 0x00, 0x00));
			} else if	( balance > 0 ) {
				amount.setTextColor(Color.rgb(0x00, 0xc0, 0x00));
			} else {
				amount.setTextColor(Color.rgb(0xcf, 0xc0, 0x00));
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
			amount.setText(valueString.toString());
		}
	}	
}