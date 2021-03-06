package com.funkyandroid.banking.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BackupUtils;

public class BackupActivity extends ActionBarActivity {

	private final static String[] NAME_ID_COLS = { "_id", "name" };

	private final static String[] SETTINGS_COLS = { "name", "value" };

	private final static String[] ACCOUNTS_COLS = { "_id", "opening_balance", "balance", "name", "currency" };

	private final static String[] ENTRIES_COLS =
		{	"_id", "account_id", "category_id", "payee_id", "link_id",
			"timestamp", "type", "amount" };

	/**
	 * The status box.
	 */

	private TextView status;

	/**
	 * The handler for posting updates to the status.
	 */

	private final Handler handler = new Handler();

	/**
	 * The Database connection
	 */

	private SQLiteDatabase db;

	/**
	 * The path for the backup
	 */

	private String path;

	/**
	 * The password for the backup
	 */

	private String password;

    /**
     * Whether or not external storage is enabled
     */

    private boolean mExternalStorageEnabled;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle(R.string.titleBackup);
        setContentView(R.layout.backup);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        status = (TextView) findViewById(R.id.status);
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mExternalStorageEnabled = true;
        } else {
            new AlertDialog.Builder(this)
            		.setTitle("Missing Memory Card")
            		.setMessage("A memory card is required to perform a backup")
            		.setIcon(android.R.drawable.ic_dialog_alert)
            		.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							BackupActivity.this.finish();
						}
            		})
            		.show();
            mExternalStorageEnabled = false;
            status.setText("ERROR : No memory card found. Backup unavailable");
        	return;
        }

    	db = (new DBHelper(this)).getReadableDatabase();
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
        getMenuInflater().inflate(R.menu.backup_menu, menu);
        menu.findItem(R.id.menu_backup).setEnabled(mExternalStorageEnabled);
		super.onCreateOptionsMenu(menu);
		return true;
	}

    /**
     * Update the status window.
     */

    private void updateStatus( final String statusUpdate ) {
    	handler.post( new StatusUpdater(statusUpdate) );
    }

    /**
     * Start the backup
     */

    private void startBackup() {
    	try {
	    	EditText editText = (EditText) findViewById(R.id.name);
	    	String name = editText.getText().toString();
	    	path = getBackupFilename(name);

	    	editText = (EditText) findViewById(R.id.password);
	    	password = editText.getText().toString();

	    	if(path == null) {
	    		reportNoBackupDirectory();
	    		return;
	    	}

	    	if(new File(path).exists()) {
	    		reportExistingFile();
	    		return;
	    	}

	    	startBackupThread();
    	} catch(Exception ex) {
            new AlertDialog.Builder(this)
	    		.setTitle("Backup Failed")
	    		.setMessage("The backup can't be started : "+ex.getMessage())
	    		.setIcon(android.R.drawable.ic_dialog_alert)
	    		.setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						BackupActivity.this.finish();
					}
	    		})
	    		.show();
    	}
    }

    /**
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
            case R.id.menu_backup:
                startBackup();
                return true;

	    	case android.R.id.home:
				finish();
				return true;

	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

    /**
     * Create the filename for tha backup
     * @throws IOException
     */

    private String getBackupFilename(final String name) throws IOException {
		StringBuilder path = new StringBuilder();
		BackupUtils.addBackupPath(path);
		File backupDir = new File(path.toString());
		if(!backupDir.exists() && !backupDir.mkdir()) {
			return null;
		}

		path.append('/');
		path.append(name);
		path.append(".fex");
		return path.toString();
    }

    /**
     * Report that the backup directory could not be created
     */

    private void reportExistingFile() {
        new AlertDialog.Builder(this)
		.setTitle("Overwrite Backup?")
		.setMessage("A backup with that name already exists. Do you want to overwrite it?")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
		    	startBackupThread();
			}
		})
		.setNegativeButton("No", null)
		.show();
    }


    /**
     * Start the backup
     */

    private void startBackupThread() {
    	try {
	    	new Thread(new Backupper(path, password)).start();
    	} catch(Exception ex) {
            new AlertDialog.Builder(this)
	    		.setTitle("Backup Failed")
	    		.setMessage("The backup can't be started : "+ex.getMessage())
	    		.setIcon(android.R.drawable.ic_dialog_alert)
	    		.setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						BackupActivity.this.finish();
					}
	    		})
	    		.show();
    	}
    }

    /**
     * Report that the backup directory could not be created
     */

    private void reportNoBackupDirectory() {
        new AlertDialog.Builder(this)
		.setTitle("Backup Failed")
		.setMessage("The backup could not be created. Please check your memory card for problems.")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				BackupActivity.this.finish();
			}
		})
		.show();
    }

    /**
     * Class to perform the backup
     */

    private class Backupper implements Runnable {

    	/**
    	 * The name of the backup.
    	 */
    	private final String path;

    	/**
    	 * The cipher to encrypt the data with.
    	 */

    	private final Cipher cipher;

    	/**
    	 * The stream the data is being written to.
    	 */
    	private FileOutputStream fos;

    	/**
    	 * The list of bytes waiting to be encrypted.
    	 */
    	private final List<byte[]> waitingBytes = new ArrayList<byte[]>();

    	Backupper(final String path, final String password)
    		throws NoSuchAlgorithmException, NoSuchPaddingException,
    		InvalidKeyException, UnsupportedEncodingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
    		this.path = path;
    		cipher = BackupUtils.getCipher(password, Cipher.ENCRYPT_MODE);
    	}

    	/**
    	 * Perform the backup.
    	 */

    	@Override
		public void run() {
    		updateStatus("Backup Started");
    		try {
    			final File file = new File(path);
				fos = new FileOutputStream(file);
			    try {
    				add(BackupUtils.BACKUP_HEADER);
    				writeEncryptedData();
    				backupCategories();
    				backupPayees();
    				backupSettings();
    				backupAccounts();
    				backupEntries();
    	    		updateStatus("Backup Completed.");
    			} finally {
    				fos.close();
    			}
    		} catch(Exception ex) {
    			updateStatus("Error : "+ex.getMessage());
                Log.e("Backup", "Error", ex);
    		}
    	}

    	/**
    	 * Encrypt a string
    	 * @throws UnsupportedEncodingException
    	 */

    	private void add(final String string) throws UnsupportedEncodingException {
			byte[] stringBytes  = string.getBytes("UTF-8");
			byte[] lengthBytes = new byte[4];
			BackupUtils.serialize(stringBytes.length, lengthBytes, 0);
			waitingBytes.add(lengthBytes);
			waitingBytes.add(stringBytes);
    	}

    	/**
    	 * Encrypt an integer
    	 * @throws UnsupportedEncodingException
    	 */

    	private void add(final int data) throws UnsupportedEncodingException {
    		byte[] dataBytes = new byte[4];
    		BackupUtils.serialize(data, dataBytes, 0);
			waitingBytes.add(dataBytes);
    	}

    	/**
    	 * Encrypt a long
    	 * @throws UnsupportedEncodingException
    	 */

    	private void add(final long data) throws UnsupportedEncodingException {
    		byte[] dataBytes = new byte[8];
			BackupUtils.serialize(data, dataBytes, 0);
			waitingBytes.add(dataBytes);
    	}

    	/**
    	 * Writes data in a cipher to the output stream
    	 * @throws BadPaddingException
    	 * @throws IllegalBlockSizeException
    	 * @throws IOException
    	 */

    	private void writeEncryptedData()
    		throws IllegalBlockSizeException, BadPaddingException, IOException {

    		int totalLength = 0;
    		for( byte[] array : waitingBytes ) {
    			totalLength += array.length;
    		}

    		byte[] data = new byte[totalLength];
    		int position = 0;
    		for( byte[] array : waitingBytes ) {
    			System.arraycopy(array, 0, data, position, array.length);
    			position += array.length;
    		}

			byte[] encryptedData = cipher.doFinal(data);
    		byte[] lengthData = new byte[4];
			BackupUtils.serialize(encryptedData.length, lengthData, 0);
			fos.write(lengthData);
			fos.write(encryptedData);

			waitingBytes.clear();
    	}

    	/**
    	 * Backup the categories
    	 *
    	 * @throws IOException
    	 * @throws BadPaddingException
    	 * @throws IllegalBlockSizeException
    	 */

    	private void backupCategories()
    		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Backing up categories....");
    		backupIDNameTable(DBHelper.CATEGORIES_TABLE_NAME);
    	}

    	/**
    	 * Backup the payees
    	 *
    	 * @throws IOException
    	 * @throws BadPaddingException
    	 * @throws IllegalBlockSizeException
    	 */

    	private void backupPayees()
    		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Backing up payees....");
    		backupIDNameTable(DBHelper.PAYEE_TABLE_NAME);
    	}

    	/**
    	 * Backup a table with an id and name in it.
    	 *
    	 * @param table The table to backup.
         *
    	 * @throws IOException
    	 * @throws BadPaddingException
    	 * @throws IllegalBlockSizeException
    	 */

    	private void backupIDNameTable(final String table)
		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		Cursor cursor = db.query(	table,
    									BackupActivity.NAME_ID_COLS,
    									null, null, null, null, null);
    		try {
    			add(cursor.getCount());
    			writeEncryptedData();
    			while(cursor.moveToNext()) {
    				add(cursor.getInt(0));
    				if(cursor.isNull(1)) {
    					add(0);
    				} else {
    					add(cursor.getString(1));
    				}

    				writeEncryptedData();
    			}
    		} finally {
    			cursor.close();
    		}
    	}

    	/**
    	 * Backup a table with an id and name in it.
    	 *
    	 * @throws IOException
    	 * @throws BadPaddingException
    	 * @throws IllegalBlockSizeException
    	 */

    	private void backupSettings()
		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Backing up settings....");
    		Cursor cursor = db.query(	DBHelper.SETTINGS_TABLE_NAME,
    									BackupActivity.SETTINGS_COLS,
    									null, null, null, null, null);
    		try {
    			add(cursor.getCount());
    			writeEncryptedData();
    			while(cursor.moveToNext()) {
    				if( cursor.isNull(0) || cursor.isNull(1)) {
    					continue;
    				}

    				add(cursor.getString(0));
    				add(cursor.getString(1));
    				writeEncryptedData();
    			}
    		} finally {
    			cursor.close();
    		}
    	}

    	/**
    	 * Backup a table with an id and name in it.
    	 *
    	 * @throws IOException
    	 * @throws BadPaddingException
    	 * @throws IllegalBlockSizeException
    	 */

    	private void backupAccounts()
		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Backing up accounts....");
    		Cursor cursor = db.query(	DBHelper.ACCOUNTS_TABLE_NAME,
    									BackupActivity.ACCOUNTS_COLS,
    									null, null, null, null, null);
    		try {
    			add(cursor.getCount());
    			writeEncryptedData();
    			while(cursor.moveToNext()) {
    				add(cursor.getInt(0));
    				add(cursor.getLong(1));
    				add(cursor.getLong(2));
    				add(cursor.getString(3));
    				add(cursor.getString(4));
    				writeEncryptedData();
    			}
    		} finally {
    			cursor.close();
    		}
    	}

    	/**
    	 * Backup a table with an id and name in it.
    	 *
    	 * @throws IOException
    	 * @throws BadPaddingException
    	 * @throws IllegalBlockSizeException
    	 */

    	private void backupEntries()
		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Backing up entries....");
    		Cursor cursor = db.query(	DBHelper.ENTRIES_TABLE_NAME,
    									BackupActivity.ENTRIES_COLS,
    									null, null, null, null, null);
    		try {
    			add(cursor.getCount());
    			writeEncryptedData();
    			while(cursor.moveToNext()) {
    				add(cursor.getInt(0));
    				add(cursor.getInt(1));
    				add(cursor.getInt(2));
    				add(cursor.getInt(3));
    				add(cursor.getInt(4));
    				add(cursor.getLong(5));
    				add(cursor.getInt(6));
    				add(cursor.getLong(7));
					writeEncryptedData();
    			}
    		} finally {
    			cursor.close();
    		}
    	}
    }


    /**
     * Status updater runnable.
     */

    private class StatusUpdater implements Runnable {
    	/**
    	 * The update.
    	 */

    	private final String statusUpdate;

    	StatusUpdater(final String statusUpdate) {
    		this.statusUpdate = statusUpdate;
    	}

    	@Override
		public void run() {
    		status.setText(statusUpdate);
    	}
    }
}
