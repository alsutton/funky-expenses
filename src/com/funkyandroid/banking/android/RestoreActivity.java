package com.funkyandroid.banking.android;

import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class RestoreActivity extends SherlockActivity {

	/**
	 * The status box.
	 */

	private TextView status;

	/**
	 * The Database connection
	 */

	private SQLiteDatabase db;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle(R.string.titleRestore);
        setContentView(R.layout.restore);

        ((Button) findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				RestoreActivity.this.finish();
			}
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        status = (TextView) findViewById(R.id.status);
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
		MenuUtil.buildMenu(this, menu);
		return super.onCreateOptionsMenu(menu);
	}

    /**
     * Start the backup
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
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