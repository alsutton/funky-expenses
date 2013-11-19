package com.funkyandroid.banking.android;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class CategoriesReportActivity
	extends ActionBarActivity
	implements DatabaseReadingActivity {

	/**
	 * The parameter used to pass the intent extras between instances.
	 */

	private static final String INTENT_EXTRAS_STRING = "I_EXTRAS";

	/**
	 * The connection to the database.
	 */

	private SQLiteDatabase database;

	/**
	 * The currency symbol
	 */

	private CategoriesReportFragment categoriesReportFragment;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_layout);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    	database = (new DBHelper(this)).getWritableDatabase();

    	Bundle extras = getIntent().getExtras();
    	if(extras == null) {
    		extras = savedInstanceState.getBundle(INTENT_EXTRAS_STRING);
    	}

    	categoriesReportFragment = new CategoriesReportFragment();
    	categoriesReportFragment.setArguments(extras);
        getSupportFragmentManager().beginTransaction()
		        .add(R.id.fragment_holder, categoriesReportFragment)
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
     * When resuming check the title bar shows the category name
     */

    @Override
    public void onResume() {
    	super.onResume();
    	getSupportActionBar().setTitle(categoriesReportFragment.account.name);
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

		MenuUtil.buildMenu(this, menu);

		return true;
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
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

    /**
     * Get the database connection. Used by the Fragment
     */
	@Override
	public SQLiteDatabase getReadableDatabaseConnection() {
		return database;
	}
}