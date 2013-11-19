package com.funkyandroid.banking.android;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v7.app.ActionBarActivity;


import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;

public class CategoryReportActivity
        extends ActionBarActivity
        implements DatabaseReadingActivity {
    /**
     * The parameter used to pass the intent extras between instances.
     */

    private static final String INTENT_EXTRAS_STRING = "I_EXTRAS";

    /**
	 * The connection to the database.
	 */

	private SQLiteDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.fragment_layout);

        db = (new DBHelper(this)).getReadableDatabase();

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            extras = savedInstanceState.getBundle(INTENT_EXTRAS_STRING);
        }

        CategoryReportFragment fragment = new CategoryReportFragment();
        fragment.setArguments(extras);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_holder, new AccountsFragment())
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            outState.putBundle(INTENT_EXTRAS_STRING, extras);
        }
    }

    /**
     * Close database connection onDestroy.
     */

    @Override
	public void onDestroy() {
    	if(db != null && db.isOpen()) {
            db.close();
    	}
    	super.onDestroy();
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

    @Override
    public SQLiteDatabase getReadableDatabaseConnection() {
        return db;
    }
}