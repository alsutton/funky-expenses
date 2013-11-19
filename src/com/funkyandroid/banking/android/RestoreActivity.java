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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class RestoreActivity extends ActionBarActivity {

	/**
	 * The Database connection
	 */

	private SQLiteDatabase db;

	/**
	 * The broadcaster receiver for progress updates.
	 */

	private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			RestoreActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int percentage = intent.getIntExtra("progress", 0);
					if(percentage < 100) {
						StringBuilder string = new StringBuilder();
						string.append(RestoreActivity.this.getText(R.string.restoreIs));
						string.append(' ');
						string.append(percentage);
						string.append('%');
						string.append(RestoreActivity.this.getText(R.string.restoreComplete));
						((TextView)RestoreActivity.this.findViewById(R.id.status)).setText(string.toString());
					} else {
						((TextView)RestoreActivity.this.findViewById(R.id.status)).setText(RestoreActivity.this.getString(R.string.restoreCompleted));
					}
				}
			});
		}

	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle(R.string.titleRestore);
        setContentView(R.layout.restore);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				RestoreActivity.this.finish();
			}
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        final TextView status = (TextView) findViewById(R.id.status);
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
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
            okButton.setEnabled(false);
            status.setText("ERROR : No memory card found. Restore unavailable");
        	return;
        }

        okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				startRestore();
			}
        });
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
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case android.R.id.home:
	    	{
				finish();
				return true;
	    	}
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
		MenuUtil.buildMenu(this, menu);
		return super.onCreateOptionsMenu(menu);
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