package com.funkyandroid.banking.android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class AccountsActivity extends Activity {
	
	/**
	 * The list adapter holding the accounts list.
	 */
	
	private AccountsListAdapter adapter;

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
		
		MenuUtil.buildMenu(this, menu);
		
		return true;
	}
}