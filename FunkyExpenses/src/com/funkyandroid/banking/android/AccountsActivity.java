package com.funkyandroid.banking.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.ui.keypad.KeypadFactory;
import com.funkyandroid.banking.android.ui.keypad.KeypadHandler;
import com.funkyandroid.banking.android.utils.Crypto;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class AccountsActivity extends Activity 
	implements KeypadHandler.OnOKListener {
	
	/**
	 * The list adapter holding the accounts list.
	 */
	
	private AccountsListAdapter adapter;

	/**
	 * The handler for showing keypads.
	 */
	
	private KeypadHandler keypadHandler = null;
	
	/**
	 * The first password entered in the change password box.
	 */
	
	private String password1;
	
	/**
	 * The database connection
	 */
	
	private SQLiteDatabase db;
	
	/**
	 * The handler for the post onCreate stuff.
	 */
	
	private Handler handler = new Handler();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.accounts);

        db = (new DBHelper(this)).getWritableDatabase();        
        Cursor accounts = AccountManager.getAll(db);
        startManagingCursor(accounts);
		adapter = new AccountsListAdapter(AccountsActivity.this, accounts);
		
        ListView list = (ListView)findViewById(R.id.informationList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(adapter);
        list.setOnItemLongClickListener(adapter);

        Button button = (Button) findViewById(R.id.add);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					Intent viewIntent = new Intent(AccountsActivity.this, EditAccountActivity.class);
        					AccountsActivity.this.startActivity(viewIntent);    				    	
        				}
        		});
        handler.post(new PostCreateThread());
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

		menu.add(R.string.menuChangePassword)
			.setIcon(android.R.drawable.ic_menu_view)
			.setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(final MenuItem item) {
						showSetPassword();
			            return true;						
					}
				}
			);
	
		menu.add(R.string.menuExport)
		.setIcon(android.R.drawable.ic_menu_save)
		.setOnMenuItemClickListener(
			new OnMenuItemClickListener() {
				public boolean onMenuItemClick(final MenuItem item) {
					Intent viewIntent = new Intent(AccountsActivity.this, BackupActivity.class);
					AccountsActivity.this.startActivity(viewIntent);    				    	
		            return true;						
				}
			}
		);

		menu.add(R.string.menuRestore)
		.setIcon(android.R.drawable.ic_menu_upload)
		.setOnMenuItemClickListener(
			new OnMenuItemClickListener() {
				public boolean onMenuItemClick(final MenuItem item) {
					Intent viewIntent = new Intent(AccountsActivity.this, RestoreActivity.class);
					AccountsActivity.this.startActivity(viewIntent);    				    	
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
    	synchronized(this) {
	    	if( keypadHandler == null ) {         
		    	keypadHandler = KeypadFactory.getKeypadHandler(this); 
	    	}
    	}
	    keypadHandler.display(1, R.string.setPassword, "", this, false);
    }
    
    /**
     * Handle a password change.
     */
    
    public void onOK(final int id, final String password) {
    	if( id == 1 ) {
    		password1 = password;
        	keypadHandler.display(2, R.string.newPasswordConfirm, "", this, false);
        	return;
    	}
    	
		boolean match;
		if			( password1 == null && password == null) {
			match = true;
		} else if	( password1 == null || password == null ) {
			match = false;
		} else {
			match = password1.equals(password);
		}

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
    
    
    /**
     * A post onCreate initialisation thread.
     */
    
    private class PostCreateThread implements Runnable {
    	public void run() {            
            if( AccountsActivity.this.adapter.isEmpty() ) {
            	Toast.makeText(AccountsActivity.this, "Press menu to add an account", Toast.LENGTH_LONG).show();
            } else {
            	Toast.makeText(AccountsActivity.this, "Tap an account to view or add entries.\nPress and hold to edit account details.", Toast.LENGTH_LONG).show();
            	
            }
    	}
    }
}