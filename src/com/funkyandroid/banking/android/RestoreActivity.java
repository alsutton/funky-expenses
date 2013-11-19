package com.funkyandroid.banking.android;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;

public class RestoreActivity extends ActionBarActivity {

	/**
	 * The Database connection
	 */

	private SQLiteDatabase db;

    /**
     * Whether or not external storage is available.
     */

    private boolean mExternalStorageEnabled;

	/**
	 * The broadcaster receiver for progress updates.
	 */

	private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
            if(intent.hasExtra("error")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(getText(R.string.restoreAborted));
                        stringBuilder.append("\n\n");
                        stringBuilder.append(intent.getStringExtra("error"));
                        ((TextView)findViewById(R.id.status)).setText(stringBuilder.toString());
                    }
                });
            } else {
			    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int percentage = intent.getIntExtra("progress", 0);
                        if (percentage < 100) {
                            StringBuilder string = new StringBuilder();
                            string.append(RestoreActivity.this.getText(R.string.restoreIs));
                            string.append(' ');
                            string.append(percentage);
                            string.append('%');
                            string.append(getText(R.string.restoreComplete));
                            ((TextView) findViewById(R.id.status)).setText(string.toString());
                        } else {
                            ((TextView) findViewById(R.id.status)).setText(RestoreActivity.this.getString(R.string.restoreCompleted));
                        }
                    }
                });
            }
		}

	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle(R.string.titleRestore);
        setContentView(R.layout.restore);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final TextView status = (TextView) findViewById(R.id.status);
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mExternalStorageEnabled = true;
        } else {
            new AlertDialog.Builder(this)
            		.setTitle("Missing Memory Card")
            		.setMessage("A memory card is required to restore data from.")
            		.setIcon(android.R.drawable.ic_dialog_alert)
            		.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							RestoreActivity.this.finish();
						}
            		})
            		.show();
            mExternalStorageEnabled = false;
            status.setText("ERROR : No memory card found. Restore unavailable");
        	return;
        }

    	db = (new DBHelper(this)).getWritableDatabase();
    }

    /**
     * Setup the broadcast receiver in onResume
     */

    @Override
    public void onResume() {
    	super.onResume();
    	registerReceiver(updateReceiver, new IntentFilter(RestoreService.PROGRESS_UPDATE_BROADCAST));
    }

    /**
     * Stop the broadcast receiver onPause.
     */

    @Override
    public void onPause() {
    	super.unregisterReceiver(updateReceiver);
    	super.onPause();
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
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.restore_menu, menu);
        menu.findItem(R.id.menu_restore).setEnabled(mExternalStorageEnabled);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /**
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
            case R.id.menu_restore:
                startRestore();
                return true;

	    	case android.R.id.home:
				finish();
				return true;

	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

    /**
     * Start the backup
     */

    private void startRestore() {
    	try {
	    	EditText editText = (EditText) findViewById(R.id.name);
	    	String name = editText.getText().toString();

	    	editText = (EditText) findViewById(R.id.password);
	    	String password = editText.getText().toString();

	    	Intent startIntent = new Intent(this, RestoreService.class);
	    	startIntent.putExtra(RestoreService.BACKUP_NAME_BUNDLE_PARAM, name);
	    	startIntent.putExtra(RestoreService.BACKUP_PASSWORD_BUNDLE_PARAM, password);
	    	startService(startIntent);
    	} catch(Exception ex) {
            new AlertDialog.Builder(this)
	    		.setTitle("Restore Failed")
	    		.setMessage("The restore can't be started : "+ex.getMessage())
	    		.setIcon(android.R.drawable.ic_dialog_alert)
	    		.setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						RestoreActivity.this.finish();
					}
	    		})
	    		.show();
    	}
    }
}