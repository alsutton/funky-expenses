package com.funkyandroid.banking.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
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
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class EntriesActivity extends ListActivity {

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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

		((TextView) findViewById(R.id.title)).setText(account.name);

		((Button) findViewById(R.id.add))
        	.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					final Context context = EntriesActivity.this;
        					Intent intent = new Intent(context, EditEntryActivity.class);
        					intent.putExtra("com.funkyandroid.banking.account_id", account.id);
        					intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
        					context.startActivity(intent);
        				}
        		});
		((Button) findViewById(R.id.categories_button))
	        	.setOnClickListener(
	        		new View.OnClickListener() {
	        				public void onClick(final View view) {
	        					startCategories();
	        				}
	        		});

        final Cursor entryCursor = TransactionManager.getForAccount(database, accountId);
    	startManagingCursor(entryCursor);
    	setListAdapter(new MyListAdapter(entryCursor));
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
		((MyListAdapter)getListAdapter()).notifyDataSetChanged();
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
						startCategories();
			            return true;
					}
				}
			);

		menu.add(R.string.menuEmail)
		.setIcon(android.R.drawable.ic_menu_send)
		.setOnMenuItemClickListener(
			new OnMenuItemClickListener() {
				public boolean onMenuItemClick(final MenuItem item) {
			        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				    	new Thread(new MyExporter()).start();
			        } else {
			            new AlertDialog.Builder(EntriesActivity.this)
			            		.setTitle("Missing Memory Card")
			            		.setMessage("A memory card is required to export your data")
			            		.setIcon(android.R.drawable.ic_dialog_alert)
			            		.setPositiveButton("OK", null)
			            		.show();
			        }
		            return true;
				}
			}
		);

		MenuUtil.buildMenu(this, menu);

		return true;
	}

	/**
	 * Handle clicks by opening a the entry editor
	 */
    @Override
	public void onListItemClick(final ListView list, final View view, int position, long id) {
		Intent intent = new Intent(this, EditEntryActivity.class);
		intent.putExtra("com.funkyandroid.banking.transaction_id", ((int)id&0xffffff));
		intent.putExtra("com.funkyandroid.banking.account_currency", currencySymbol);
		startActivity(intent);
	}

    /**
     * Start the category report activity
     */

    private void startCategories() {
		Intent intent = new Intent(this, SpendingReportActivity.class);
		intent.putExtra("com.funkyandroid.banking.account_id", account.id);
		startActivity(intent);
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
    		super(EntriesActivity.this, R.layout.entry_list_item, c);
    	}

    	/**
    	 * Populate an entry view with the data from a cursor.
    	 */

    	@Override
    	public void bindView(final View view, final Context context, final Cursor cursor) {
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

    		Date entryDate = new Date(cursor.getLong(3));
    		((TextView)view.findViewById(R.id.date)).setText(sdf.format(entryDate));
    	}
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
				File exportFile = new File(Environment.getExternalStorageDirectory(), "export.csv");
				if(exportFile.exists()) {
					exportFile.delete();
				}
				exportFile.createNewFile();
				Cursor exportCursor = TransactionManager.getForExportForAccount(database, account.id);
		    	try {
		    		PrintStream ps = new PrintStream(new FileOutputStream(exportFile));
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
		    	sendIntent.setType("text/csv");
		    	sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Funky Expenses Export");
		   	    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportFile));

		   	    startActivity(Intent.createChooser(sendIntent, "Send using..."));
		    } catch(Exception ex) {
				Log.e("FExpenses", "CSV Export", ex);
		    }
	    }
	}
}
