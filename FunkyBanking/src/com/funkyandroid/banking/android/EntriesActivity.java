package com.funkyandroid.banking.android;

import java.util.Currency;

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
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class EntriesActivity extends Activity {
	
	/**
	 * The adapter holding the entry information.
	 */
	
	private EntriesListAdapter adapter;
	
	/**
	 * The ID of the account being viewed.
	 */
	
	private Account account;

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
        setContentView(R.layout.entries);
    	
    	Intent startingIntent = getIntent();    	
    	int accountId = startingIntent.getIntExtra("com.funkyandroid.banking.account_id", -1);
    	if( accountId == -1 ) {
    		finish();
    		return;
    	}

    	database = (new DBHelper(this)).getReadableDatabase();
    	account = AccountManager.getById(database, accountId);
    	if( account == null ) {
    		database.close();
    		finish();
    		return;    		
    	}
        
		Currency currency = Currency.getInstance(account.getCurrency());
		if( currency == null ) {
			currencySymbol = BalanceFormatter.UNKNOWN_CURRENCY_SYMBOL;
		} else {
			currencySymbol = currency.getSymbol();
		}
    	    	
		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(account.getName());

		Button button = (Button) findViewById(R.id.add);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					Context context = EntriesActivity.this;
        					Intent intent = new Intent(context, EditEntryActivity.class);
        					intent.putExtra("com.funkyandroid.banking.account_id", account.getId());
        					intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
        					context.startActivity(intent);    				
        				}
        		});

        Cursor entryCursor = TransactionManager.getForAccount(database, accountId);
    	startManagingCursor(entryCursor);
    	
    	adapter = new EntriesListAdapter(this, entryCursor, currencySymbol); 
    	ListView list = (ListView) findViewById(R.id.informationList);
    	list.setAdapter(adapter);
    	list.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			Intent intent = new Intent(view.getContext(), EditEntryActivity.class);
    			intent.putExtra("com.funkyandroid.banking.transaction_id", ((int)id&0xffffff));
				intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
    			view.getContext().startActivity(intent);    				
    		}    		
    	});
    }

    /**
     * Called whenever the activity becomes visible.
     */
    
    @Override
    public void onStart() {
    	super.onStart();
    	account = AccountManager.getById(database, account.getId());
    	updateBalance(account.getBalance());
		adapter.notifyDataSetChanged();
    }

    /**
     * Close database connection onDestroy.
     */
    
    public void onDestroy() {
    	database.close();
    	super.onDestroy();
    }
    
    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
			     
		menu.add(R.string.newEntry)
		.setIcon(android.R.drawable.ic_menu_add)
		.setOnMenuItemClickListener(
			new OnMenuItemClickListener() {
				public boolean onMenuItemClick(final MenuItem item) {
					Context context = EntriesActivity.this;
					Intent intent = new Intent(context, EditEntryActivity.class);
					intent.putExtra("com.funkyandroid.banking.account_id", account.getId());
					intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
					context.startActivity(intent);    				
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
		if			( newBalance < 0 ) {
			textView.setTextColor(Color.rgb(0xc0, 0x00, 0x00));
		} else if	( newBalance > 0 ) {
			textView.setTextColor(Color.rgb(0x00, 0xc0, 0x00));
		} else {
			textView.setTextColor(Color.rgb(0xcf, 0xc0, 0x00));
		}
    	textView.setText(balanceText.toString());
    }
}