package com.funkyandroid.banking.android.expenses.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import com.funkyandroid.banking.android.AccountsActivity;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.utils.Crypto;

public class Launcher extends Activity {

	/**
	 * The tag used for logging
	 */

	public static final String LOG_TAG = "FunkyExpenses";

	/**
	 * The shared preferences.
	 */

	private String passwordHash;

    /**
     * The password field
     */

    private EditText passwordEntry;

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

            View body = getLayoutInflater().inflate(R.layout.dialog_enter_password, null);
            passwordEntry = (EditText) body.findViewById(R.id.password);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.enterPasswordTitle)
                    .setView(body)
                    .setCancelable(false)
                    .setPositiveButton(
                        android.R.string.ok,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String password = passwordEntry.getText().toString();
                                if( password != null && password.length() > 0 ) {
                                    boolean passwordOK = false;
                                    try {
                                        passwordOK = Crypto.getHash(password).equals(passwordHash);
                                    } catch(Exception ex) {
                                        Log.e(LOG_TAG, "Problem generating hash.", ex);
                                    }
                                    if( passwordOK ) {
                                        startAccountsActivity();
                                        return;
                                    }
                                }
                                new AlertDialog.Builder(Launcher.this).setTitle("Password Incorrect")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setMessage("The password you entered was not correct.")
                                        .setPositiveButton("OK", new OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            }
                        }
                    ).show();
    	} catch( Exception ex ) {
            new AlertDialog.Builder(this).setTitle("Problem during startup")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Please report the following to support@funkyandroid.com : "+ex.getMessage())
            .setPositiveButton("OK", new OnClickListener() {
    			@Override
				public void onClick(DialogInterface dialog, int which) {
    		    	finish();
    			}
            })
            .show();
    	}
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
