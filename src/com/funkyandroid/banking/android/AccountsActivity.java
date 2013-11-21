package com.funkyandroid.banking.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.AboutUtil;
import com.funkyandroid.banking.android.utils.Crypto;

public class AccountsActivity extends ActionBarActivity
	implements DatabaseReadingActivity {

	/**
	 * The database connection
	 */

	private SQLiteDatabase db;

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
        View body = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.setPassword)
                .setView(body)
                .setPositiveButton(android.R.string.ok, new SetPasswordListener(body))
                .show();
    }

    /**
     * Support classes.
     */

    private class SetPasswordListener implements DialogInterface.OnClickListener {

        private TextView password1View, password2View;

        private SetPasswordListener(View dialogBody) {
            super();
            password1View = (TextView)dialogBody.findViewById(R.id.password1);
            password2View = (TextView)dialogBody.findViewById(R.id.password2);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String    password1 = password1View.getText().toString(),
                            password2 = password2View.getText().toString();

            boolean match = (password1 == null && password2 == null)
                         || (password1 != null && password2 != null && password1.equals(password2));

            if( !match ) {
                new AlertDialog.Builder(AccountsActivity.this).setTitle("Password NOT Updated")
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
                new AlertDialog.Builder(AccountsActivity.this).setTitle("Password NOT Updated")
                        .setMessage("Your password could not be updated at the current time")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            if( password1 == null || password1.length() == 0 ) {
                new AlertDialog.Builder(AccountsActivity.this).setTitle("Password Removed")
                        .setMessage("You can now access your accounts without a password.")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                new AlertDialog.Builder(AccountsActivity.this).setTitle("Password Updated")
                        .setMessage("Your password has been updated")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    };
}