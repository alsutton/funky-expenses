package com.funkyandroid.banking.android;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;

public class ExternalEntryActivity
       extends ActionBarActivity
       implements DatabaseReadingActivity {

    /**
     * The parameter used to pass the intent extras between instances.
     */

    private static final String INTENT_EXTRAS_STRING = "I_EXTRAS";

    /**
	 * The Database connection
	 */

	private SQLiteDatabase db;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_layout);

    	db = (new DBHelper(this)).getReadableDatabase();

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            extras = savedInstanceState.getBundle(INTENT_EXTRAS_STRING);
        }

        ExternalEntryFragment externalEntryFragment = new ExternalEntryFragment();
        externalEntryFragment.setArguments(extras);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_holder, externalEntryFragment)
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
     * Override onDestroy to close the database.
     */

    @Override
	public void onDestroy() {
    	super.onDestroy();
    	if( db != null && db.isOpen() ) {
    		db.close();
    	}
    }

    @Override
    public SQLiteDatabase getReadableDatabaseConnection() {
        return db;
    }
}