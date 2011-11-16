package com.funkyandroid.banking.android;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.MenuInflater;

import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class CategoriesReportActivity
	extends FragmentActivity
	implements DatabaseReadingActivity {

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

    	database = (new DBHelper(this)).getWritableDatabase();

    	categoriesReportFragment = new CategoriesReportFragment();
    	categoriesReportFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
		        .add(R.id.fragment_holder, categoriesReportFragment)
		        .commit();
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

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.categories_menu, menu);

		MenuUtil.buildMenu(this, menu);

		return true;
	}

    /**
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case R.id.menu_entries:
	    	{
				startEntries();
				return true;
	    	}
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

    /**
     * Start the entries activity
     */

    private void startEntries() {
		Intent intent = new Intent(this, EntriesActivity.class);
		intent.putExtra("com.funkyandroid.banking.account_id", categoriesReportFragment.account.id);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
    }

    /**
     * Get the database connection. Used by the Fragment
     */
	@Override
	public SQLiteDatabase getReadableDatabaseConnection() {
		return database;
	}
}