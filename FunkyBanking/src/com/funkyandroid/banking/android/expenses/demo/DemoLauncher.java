package com.funkyandroid.banking.android.expenses.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.funkyandroid.banking.android.AccountsActivity;
import com.funkyandroid.banking.android.KeypadHandler;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.utils.Crypto;

public class DemoLauncher extends Activity 
	implements KeypadHandler.OnOKListener {
	
	/**
	 * The shared preferences.
	 */

	private String passwordHash;	

	/**
	 * The handler for showing keypads.
	 */
	
	private KeypadHandler keypadHandler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	try {
	    	SQLiteDatabase db = (new DBHelper(this)).getWritableDatabase();
	    	try {
	    		passwordHash = SettingsManager.get(db, SettingsManager.PASSWORD_SETTING);
	    	} finally {
	    		db.close();
	    	}
	
	    	if( passwordHash == null ) {
				startAccountsActivity();
				return;
	    	}    		
	
	    	setContentView(R.layout.password_background);
	    	keypadHandler = new KeypadHandler(this);    	
	    	keypadHandler.display(1, R.string.enterPassword, "", this);
    	} catch( Exception ex ) {
            new AlertDialog.Builder(this).setTitle("Problem during startup")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Please report the following to support@funkyandroid.com : "+ex.getMessage())
            .setPositiveButton("OK", new OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    		    	DemoLauncher.this.finish();
    			}        	
            })
            .show();		    		
    	}
    }
    
    /**
     * Check the entered password
     */
    
    public void onOK(final int id, final String password) {
    	if( password != null && password.length() > 0 ) {
	    	boolean passwordOK = false;
	    	try {
	    		passwordOK = Crypto.getHash(password).equals(passwordHash);
	    	} catch(Exception ex) {
	    		Log.e("Password Check", "Problem generating hash.", ex);
	    	}
	    	if( passwordOK ) {
	    		startAccountsActivity();
	    		return;
	    	}
    	}
        new AlertDialog.Builder(this).setTitle("Password Incorrect")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage("The password you entered was not correct.")
        .setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		    	keypadHandler.display(1, R.string.enterPassword, "", DemoLauncher.this);
			}        	
        })
        .show();		
        return;
    }
    
    /**
     * Start the accounts activity
     */

    private void startAccountsActivity() {
		Intent intent = new Intent(this, AccountsActivity.class);
		startActivity(intent);
		finish();
    }
}
