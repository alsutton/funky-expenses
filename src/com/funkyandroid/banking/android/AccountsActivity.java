package com.funkyandroid.banking.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.ui.keypad.KeypadHandler;
import com.funkyandroid.banking.android.utils.Crypto;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class AccountsActivity extends SherlockFragmentActivity
	implements KeypadHandler.OnOKListener, DatabaseReadingActivity {
/* TODO
	private static final String BPK =
		"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlzlqwamobzt8MKV8R2GnKuK+"+
		"9Cj7tNXRlbCZmy4I1GBh3ig6HOT6lGHpfFtx72L1pfm2DP+Pn2PGwBIUHYmBnsidvYkbBR+"+
		"XGqXxaO+R/ug8NeXOoIDpJYANE6MY1tWyMuBIHQ3GS2LHA1rbE8sXAhGKT+oy9+"+
		"sRoWYXVibDVfy4wgHnF4L1na+BUv7/zPr9R9Tsvxl4DEq8zqRcanPGwWTrjtxGbt9XSnuUdqlAk"+
		"oOkih4VJEHuUoS9KawbePetzzzjbq18vuQK03bSnajSgsG08mXsMTpadBEdX5/P/sNkzh6ZvvPR"+
		"OE7YYB4eDztzRtdSGBNTZpXXgsVcAG25gwIDAQAB";

    private static final byte[] BS = new byte[] {
        -42, 35, 31, -28, -13, -54, 74, -62, 50, 88, -105, -45, 27, -17, -36, -13, -111, 32, -64, 9
    };
*/
    /**
	 * The license checking states
	 */
/* TODO
	private static final int LICENSE_STATE_CHECK_FAILED = -2,
							 LICENSE_STATE_CHECKING = -1,
	 						 LICENSE_STATE_UNCHECKED = 0,
							 LICENSE_STATE_CHECKED = 1;
*/

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

	/**
	 * Whether or not the license has been checked
	 */

// TODO	private final int licenseCheckStatus = LICENSE_STATE_UNCHECKED;

	/**
	 * The license checker
	 */
// TODO    private LicenseChecker licenseChecker;

    /**
     * License checker callback.
     */
// TODO    private MyLicenseCheckerCallback licenseCallback;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.fragment_layout);

        db = (new DBHelper(this)).getReadableDatabase();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_holder, new AccountsFragment())
                .commit();

/* TODO : Re-enable
		if(!"com.funkyandroid.banking.android.expenses.demo".equals(super.getApplicationContext().getPackageName())) {
	        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
	        licenseChecker = new LicenseChecker(this, new ServerManagedPolicy(this,new AESObfuscator(BS, getPackageName(), deviceId)),BPK);
			licenseCallback = new MyLicenseCheckerCallback();
		}
*/
    }

    /**
     * Close the database connection.
     */

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	db.close();
    }

    /**
     * Do a debug check when starting.
     */
    @Override
    public void onStart() {
    	super.onStart();
/* TODO: Reenable    	if(0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE )) {
	    	Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.funkyandroid.banking.android.expenses.adfree"));
	    	startActivity(myIntent);
	    	finish();
		} */
    }

    /**
     * Do a license check on each resume.
     */

    @Override
	public void onResume() {
		super.onResume();
/* TODO: Reenable
		if(!"com.funkyandroid.banking.android.expenses.demo".equals(super.getApplicationContext().getPackageName())) {
			if(licenseCheckStatus == LICENSE_STATE_UNCHECKED) {
				licenseCheckStatus = AccountsActivity.LICENSE_STATE_CHECKING;
				licenseChecker.checkAccess(licenseCallback);
			} else if (licenseCheckStatus == LICENSE_STATE_CHECK_FAILED) {
		    	Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.funkyandroid.banking.android.expenses.adfree"));
		    	startActivity(myIntent);
		    	finish();
			}
		}
*/
    }

    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.accounts_menu, menu);

		MenuUtil.buildMenu(this, menu);

		return super.onCreateOptionsMenu(menu);
	}

    /**
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {

    	case R.id.menu_new_account:
    	{
			Intent viewIntent = new Intent(AccountsActivity.this, EditAccountActivity.class);
			AccountsActivity.this.startActivity(viewIntent);
            return true;
    	}
    	case R.id.menu_change_password:
    	{
			showSetPassword();
            return true;
    	}
    	case R.id.menu_export:
    	{
			Intent viewIntent = new Intent(AccountsActivity.this, BackupActivity.class);
			AccountsActivity.this.startActivity(viewIntent);
            return true;
    	}
    	case R.id.menu_restore:
    	{
			Intent viewIntent = new Intent(AccountsActivity.this, RestoreActivity.class);
			AccountsActivity.this.startActivity(viewIntent);
            return true;
    	}

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
     * License checker callback.
     *
     * @author Al Sutton
     */
/* TODO: Reenable
    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow() {
        	licenseCheckStatus = AccountsActivity.LICENSE_STATE_CHECKED;
            return;
        }

        public void dontAllow() {
        	licenseCheckStatus = AccountsActivity.LICENSE_STATE_CHECK_FAILED;
            if (isFinishing()) {
                return;
            }
            Log.e("FunkyExpenses", "License check failed");
        	Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.funkyandroid.banking.android.expenses.adfree"));
        	startActivity(myIntent);
        	finish();
        }

        public void applicationError(ApplicationErrorCode errorCode) {
        	licenseCheckStatus = AccountsActivity.LICENSE_STATE_UNCHECKED;
            return;
        }
    }
*/
}