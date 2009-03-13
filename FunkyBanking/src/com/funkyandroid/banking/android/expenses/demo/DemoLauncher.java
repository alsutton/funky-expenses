package com.funkyandroid.banking.android.expenses.demo;

import com.funkyandroid.banking.android.AccountsActivity;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.utils.Crypto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DemoLauncher extends Activity {
	/**
	 * The shared preferences.
	 */

	private String passwordHash;	

	/**
	 * The password dialog
	 */
	
	private AlertDialog passwordDialog;
	
	/**
	 * The view in the password window.
	 */
	
	private View passwordEntryView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
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
    	showPasswordEntry(R.string.enterPassword);
    }
    
    /**
     * Show the password entry dialog
     */
    
    private void showPasswordEntry(int messageId) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);    	
    	passwordDialog = builder.create();
    	passwordEntryView = passwordDialog.getLayoutInflater().inflate(R.layout.password, null);
    	passwordDialog.setView(passwordEntryView);    	
        passwordDialog.show();
        
    	WindowManager.LayoutParams layout = passwordDialog.getWindow().getAttributes();
    	layout.width = WindowManager.LayoutParams.FILL_PARENT;
    	passwordDialog.getWindow().setAttributes(layout);
    	    	
    	TextView text = (TextView) passwordEntryView.findViewById(R.id.passwordEntryTitle);
    	text.setText(messageId);
    	
    	Button button = (Button) passwordEntryView.findViewById(R.id.cancelButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					passwordDialog.dismiss();
        					DemoLauncher.this.finish();
        				}
        		});
    	
    	button = (Button) passwordEntryView.findViewById(R.id.okButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					EditText passwordBox = 
        						(EditText) passwordEntryView.findViewById(R.id.password);
        					String password = passwordBox.getText().toString();
        					passwordDialog.dismiss();
        					checkPassword(password);
        				}
        		});
    }
    
    /**
     * Check the entered password
     */
    
    public void checkPassword(final String password) {
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
    	
    	showPasswordEntry(R.string.incorrectPassword);
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
