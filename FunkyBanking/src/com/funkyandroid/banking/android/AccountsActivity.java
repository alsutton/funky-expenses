package com.funkyandroid.banking.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.Crypto;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class AccountsActivity extends Activity {
	
	/**
	 * The list adapter holding the accounts list.
	 */
	
	private AccountsListAdapter adapter;

	/**
	 * The password setting dialog
	 */
	
	private AlertDialog setPasswordDialog;
	
	/**
	 * The view in the password window.
	 */
	
	private View passwordEntryView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts);

        Cursor accounts = AccountManager.getAll((new DBHelper(this)).getReadableDatabase());
        if( accounts.getCount() == 0 ) {
        	Toast.makeText(this, "Press menu to add an account", Toast.LENGTH_LONG).show();
        } else {
        	Toast.makeText(this, "Tap an account to view or add entries.\nPress and hold to edit account details.", Toast.LENGTH_LONG).show();
        	
        }
        startManagingCursor(accounts);
		adapter = new AccountsListAdapter(this, accounts);
		
        ListView list = (ListView)findViewById(R.id.informationList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(adapter);
        list.setOnItemLongClickListener(adapter);
    }

    /**
     * Called whenever the activity becomes visible.
     */
    
    @Override
    public void onStart() {
    	super.onStart();
		adapter.notifyDataSetChanged();
    }
    
    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
			     
		menu.add(R.string.newAccount)
			.setIcon(android.R.drawable.ic_menu_add)
			.setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(final MenuItem item) {
    					Intent viewIntent = new Intent(AccountsActivity.this, EditAccountActivity.class);
    					AccountsActivity.this.startActivity(viewIntent);    				    	
			            return true;						
					}
				}
			);

		menu.add(R.string.setPassword)
			.setIcon(android.R.drawable.ic_menu_view)
			.setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(final MenuItem item) {
						showSetPassword();
			            return true;						
					}
				}
			);
		
		MenuUtil.buildMenu(this, menu);
		
		return true;
	}
    
    /**
     * Show the set password dialog
     */
    
    private void showSetPassword() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);    	
    	setPasswordDialog = builder.create();
    	passwordEntryView = setPasswordDialog.getLayoutInflater().inflate(R.layout.set_password, null);
    	setPasswordDialog.setView(passwordEntryView);    	
    	setPasswordDialog.show();
        
    	WindowManager.LayoutParams layout = setPasswordDialog.getWindow().getAttributes();
    	layout.width = WindowManager.LayoutParams.FILL_PARENT;
    	setPasswordDialog.getWindow().setAttributes(layout);
    	    	
    	Button button = (Button) passwordEntryView.findViewById(R.id.cancelButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					setPasswordDialog.dismiss();
        				}
        		});
    	
    	button = (Button) passwordEntryView.findViewById(R.id.okButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					EditText passwordBox = 
        						(EditText) passwordEntryView.findViewById(R.id.newPasswordEntry);
        					String password = passwordBox.getText().toString();
        					passwordBox = 
        						(EditText) passwordEntryView.findViewById(R.id.newPasswordConfirmEntry);
        					String passwordConfirm = passwordBox.getText().toString();
        					setPasswordDialog.dismiss();
        					changePassword(password, passwordConfirm);
        				}
        		});
    	
    }
    
    /**
     * Handle a password change.
     */
    
    private void changePassword(final String password1, final String password2) {
		boolean match;
		if			( password1 == null && password2 == null) {
			match = true;
		} else if	( password1 == null || password2 == null ) {
			match = false;
		} else {
			match = password1.equals(password2);
		}

		if( !match ) {
	        new AlertDialog.Builder(this).setTitle("Password NOT Updated")
	        	.setIcon(android.R.drawable.ic_dialog_alert)
	            .setMessage("The specified passwords did not match. Your password has not been updated.")
	            .setPositiveButton("OK", null)
	            .show();		
			return;
		}
    	
    	SQLiteDatabase db = (new DBHelper(this)).getWritableDatabase();
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
    		
    	} finally {
    		db.close();
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