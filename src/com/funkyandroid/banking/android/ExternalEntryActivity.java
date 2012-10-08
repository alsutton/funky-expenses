package com.funkyandroid.banking.android;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class ExternalEntryActivity
       extends SherlockFragmentActivity
       implements DatabaseReadingActivity {

    /**
     * The parameter used to pass the intent extras between instances.
     */

    private static final String INTENT_EXTRAS_STRING = "I_EXTRAS";

    /**
	 * The Database connection
	 */

	private SQLiteDatabase db;

	/**
	 * The accounts spinner.
	 */

	private ExternalEntryFragment externalEntryFragment;

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

        externalEntryFragment = new ExternalEntryFragment();
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

    /**
     * Get the account details when started.
     */

    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuUtil.buildMenu(this, menu);

		return true;
	}

    @Override
    public SQLiteDatabase getReadableDatabaseConnection() {
        return db;
    }
}