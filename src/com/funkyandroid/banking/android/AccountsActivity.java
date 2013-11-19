package com.funkyandroid.banking.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.ui.keypad.KeypadHandler;
import com.funkyandroid.banking.android.utils.AboutUtil;
import com.funkyandroid.banking.android.utils.Crypto;

public class AccountsActivity extends ActionBarActivity
	implements KeypadHandler.OnOKListener, DatabaseReadingActivity {

	/**
	 * The database connection
	 */

	private SQLiteDatabase db;

	/**
	 * The handler for showing keypads.
	 */

	private KeypadHandler keypadHandler = null;

	/**
	 * The first password entered in the change password box.
	 */

	private String password1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.fragment_layout);

        db = (new DBHelper(this)).getReadableDatabase();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_holder, new AccountsFragment())
                .commit();
    }

    /**
     * Close the database connection.
     */

    @Override
    public void onDestroy() {
        db.close();
    	super.onDestroy();
    }

    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.accounts_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

    /**
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
            case R.id.menu_new_account:
                startActivity(new Intent(this, EditAccountActivity.class));
                return true;

            case R.id.menu_change_password:
                showSetPassword();
                return true;

            case R.id.menu_export:
                startActivity(new Intent(this, BackupActivity.class));
                return true;

            case R.id.menu_restore:
                startActivity(new Intent(this, RestoreActivity.class));
                return true;

            case R.id.menu_preferences:
                startActivity(new Intent(this, Preferences.class));
                return true;

            case R.id.menu_about:
                AboutUtil.showDialog(this);
                return true;

    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

	/**
	 * Get the readable database
	 */

	@Override
	public SQLiteDatabase getReadableDatabaseConnection() {
		return db;
	}

	/**
     * Show the set password dialog
     */

    private void showSetPassword() {
    	synchronized(this) {
	    	if( keypadHandler == null ) {
		    	keypadHandler = new KeypadHandler(this);
	    	}
    	}
	    keypadHandler.display(1, R.string.setPassword, "", this, false);
    }

    /**
     * Handle a password change.
     */

    @Override
	public void onOK(final int id, final String password) {
    	if( id == 1 ) {
    		password1 = password;
        	keypadHandler.display(2, R.string.newPasswordConfirm, "", this, false);
        	return;
    	}

		boolean match;
        match = (password1 == null && password == null) || (password1 != null && password != null && password1.equals(password));

		if( !match ) {
	        new AlertDialog.Builder(this).setTitle("Password NOT Updated")
	        	.setIcon(android.R.drawable.ic_dialog_alert)
	            .setMessage("The specified passwords did not match. Your password has not been updated.")
	            .setPositiveButton("OK", null)
	            .show();
			return;
		}

    	try {
			SettingsManager.set(db,
								SettingsManager.PASSWORD_SETTING,
								Crypto.getHash(password1));
    	} catch( Exception ex ) {
	        new AlertDialog.Builder(this).setTitle("Password NOT Updated")
	        .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Your password could not be updated at the current time")
            .setPositiveButton("OK", null)
            .show();
	        return;
    	}

    	if( password1 == null || password1.length() == 0 ) {
	        new AlertDialog.Builder(this).setTitle("Password Removed")
		    .setIcon(android.R.drawable.ic_dialog_info)
	        .setMessage("You can now access your accounts without a password.")
	        .setPositiveButton("OK", null)
	        .show();
    	} else {
	        new AlertDialog.Builder(this).setTitle("Password Updated")
		    .setIcon(android.R.drawable.ic_dialog_info)
	        .setMessage("Your password has been updated")
	        .setPositiveButton("OK", null)
	        .show();
    	}
    }
}