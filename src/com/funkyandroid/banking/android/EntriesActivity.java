package com.funkyandroid.banking.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BalanceFormatter;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class EntriesActivity extends SherlockFragmentActivity implements DatabaseReadingActivity {

	/**
	 * The parameter used to pass the intent extras between instances.
	 */

	private static final String INTENT_EXTRAS_STRING = "I_EXTRAS";

	/**
	 * The fragment showing the account entries.
	 */

	private EntriesFragment entries;

	/**
	 * The connection to the database.
	 */

	private SQLiteDatabase db;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_layout);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    	db = (new DBHelper(this)).getReadableDatabase();

    	Bundle extras = getIntent().getExtras();
    	if(extras == null) {
    		extras = savedInstanceState.getBundle(INTENT_EXTRAS_STRING);
    	}

    	entries = new EntriesFragment();
    	entries.setArguments(extras);
        getSupportFragmentManager().beginTransaction()
	        .add(R.id.fragment_holder, entries)
	        .commit();
    }

    /**
     * Save the intent extras if needed.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Bundle extras = getIntent().getExtras();
    	if(extras != null) {
    		outState.putBundle(INTENT_EXTRAS_STRING, extras);
    	}
    }


    /**
     * Called whenever the activity becomes visible.
     */

    @Override
    public void onStart() {
    	super.onStart();
    	entries.account = AccountManager.getById(db, entries.account.id);
    	updateBalance(entries.account.balance);
    	getSupportActionBar().setTitle(entries.account.name);
    }

    /**
     * Close database connection onDestroy.
     */

    @Override
	public void onDestroy() {
    	super.onDestroy();
    	db.close();
    }

    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.entries_menu, menu);

		MenuUtil.buildMenu(this, menu);

		return super.onCreateOptionsMenu(menu);
	}

    /**
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case android.R.id.home:
	    	{
				finish();
				return true;
	    	}
	    	case R.id.menu_new_entry:
	    	{
				Intent intent = new Intent(this, EditEntryActivity.class);
				intent.putExtra("com.funkyandroid.banking.account_id", entries.account.id);
				intent.putExtra("com.funkyandroid.banking.account_currency", entries.currencySymbol);
				startActivity(intent);
				return true;
	    	}
	    	case R.id.menu_categories:
	    	{
				startCategories();
				return true;
	    	}
	    	case R.id.menu_email_csv:
	    	{
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
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

    /**
     * Get the read only connection to the database.
     */

	@Override
	public SQLiteDatabase getReadableDatabaseConnection() {
		return db;
	}

    /**
     * Start the category report activity
     */

    private void startCategories() {
		Intent intent = new Intent(this, CategoriesReportActivity.class);
		intent.putExtra("com.funkyandroid.banking.account_id", entries.account.id);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
    }

    /**
     * Update the current account balance
     */

    public void updateBalance(long newBalance) {
    	StringBuilder balanceText = new StringBuilder(32);
    	balanceText.append("Current balance : ");
		BalanceFormatter.format(balanceText, newBalance, entries.currencySymbol);

    	TextView textView = (TextView) findViewById(R.id.balance);
    	textView.setText(balanceText.toString());
    }

    /**
     * Thread in which export will run
     */

    private class MyExporter implements Runnable {

	    /**
	     * Email a CSV of the account information out
	     */

	    @Override
		public void run() {
	    	try {
				File exportFile = new File(Environment.getExternalStorageDirectory(), "export.csv");
				if(exportFile.exists()) {
					exportFile.delete();
				}
				exportFile.createNewFile();
				Cursor exportCursor = TransactionManager.getForExportForAccount(db, entries.account.id);
		    	try {
		    		PrintStream ps = new PrintStream(new FileOutputStream(exportFile));
		    		try {
		    			ps.print("\"Date\",\"Category\",\"Payee\",\"Amount (");
		    			ps.print(entries.currencySymbol);
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
