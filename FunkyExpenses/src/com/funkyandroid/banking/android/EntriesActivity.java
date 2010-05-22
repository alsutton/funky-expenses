package com.funkyandroid.banking.android;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.flurry.android.FlurryAgent;
import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.CurrencyManager;
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
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        
		currencySymbol = CurrencyManager.getSymbol(database, account.currency); 
    	    	
		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(account.name);

		Button button = (Button) findViewById(R.id.add);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					Context context = EntriesActivity.this;
        					Intent intent = new Intent(context, EditEntryActivity.class);
        					intent.putExtra("com.funkyandroid.banking.account_id", account.id);
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
    	FlurryAgent.onStartSession(this, "8SVYESRG63PTLMNLZPPU");
    	account = AccountManager.getById(database, account.id);
    	updateBalance(account.balance);
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
					intent.putExtra("com.funkyandroid.banking.account_id", account.id);
					intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
					context.startActivity(intent);    				
		            return true;						
				}
			}
		);
		
		menu.add(R.string.menuReports)
			.setIcon(android.R.drawable.ic_menu_search)
			.setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(final MenuItem item) {
						Context context = EntriesActivity.this;
						Intent intent = new Intent(context, SpendingReportActivity.class);
						intent.putExtra("com.funkyandroid.banking.account_id", account.id);
						context.startActivity(intent);    				
			            return true;						
					}
				}
			);
		
		menu.add(R.string.menuEmail)
		.setIcon(android.R.drawable.ic_menu_send)
		.setOnMenuItemClickListener(
			new OnMenuItemClickListener() {
				public boolean onMenuItemClick(final MenuItem item) {
					emailCSV("al.sutton@alsutton.com");    				
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
     * Send a CSV export of the account to a user via email.
     */
    
    private void emailCSV(final String recipient) {
    	new Thread(new MyExporter()).start();
    }
    
    /**
     * Thread in which export will run
     */
    
    private class MyExporter implements Runnable {
    	
	    /**
	     * Email a CSV of the account information out
	     */
	    
	    public void run() {
	    	try {
				File exportFile = new File(getFilesDir(), "export.csv");
				Cursor exportCursor = TransactionManager.getForExportForAccount(database, account.id);
		    	try {
		    		PrintStream ps = new PrintStream(
		    								EntriesActivity.this.openFileOutput("export.csv", Activity.MODE_WORLD_READABLE)
		    							);
		    		try {
		    			ps.print("\"Date\",\"Category\",\"Payee\",\"Amount (");
		    			ps.print(currencySymbol);
		    			ps.println(")\"");
		    			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
		    			while(exportCursor.moveToNext()) {
		        			Date entryDate = new Date(exportCursor.getLong(0));
		        			ps.print(df.format(entryDate));
		        			ps.print(",\"");
		        			ps.print(exportCursor.getString(1));
		        			ps.print("\",\"");
		        			ps.print(exportCursor.getString(2));
		        			ps.print("\",");
		        			ps.println(BalanceFormatter.format(exportCursor.getLong(3)));
		    			}
		    		} finally {
		    			ps.close();
		    		}
		    	} finally {
		    		exportCursor.close();
		    	}
		    	
		    	Intent sendIntent = new Intent(Intent.ACTION_SEND);
		    	sendIntent.setType("image/csv");
		    	sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Funky Expenses Export");
		    	sendIntent.putExtra(Intent.EXTRA_TEXT, "Export from the account called "+account.name);
		   	    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportFile));

		   	    startActivity(Intent.createChooser(sendIntent, "Send using..."));    	
		    } catch(Exception ex) {
				Log.e("FExpenses", "CSV Export", ex);		    	
		    }
	    }
	}
}
