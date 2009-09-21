package com.funkyandroid.banking.android;

import java.io.File;
import java.io.FileNotFoundException;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.BackupUtils;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class BackupActivity extends Activity {

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
	
	private Handler handler = new Handler();
	
	/**
	 * The Database connection
	 */
	
	private SQLiteDatabase db;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle(R.string.titleBackup);
        setContentView(R.layout.backup);
        
        ((Button) findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
			public void onClick(final View view) {
				BackupActivity.this.finish();
			}        	
        });
        
        Button okButton = (Button) findViewById(R.id.okButton);
        status = (TextView) findViewById(R.id.status);
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            new AlertDialog.Builder(this)
            		.setTitle("Missing Memory Card")
            		.setMessage("A memory card is required to perform a backup")
            		.setIcon(android.R.drawable.ic_dialog_alert)
            		.setPositiveButton("OK", new OnClickListener() {
						public void onClick(final DialogInterface dialog, final int which) {
							BackupActivity.this.finish();
						}
            		})
            		.show();		
            okButton.setEnabled(false);
            status.setText("ERROR : No memory card found. Backup unavailable");
        	return;
        }
        
        okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View view) {
				startBackup();
			}
        });
    	db = (new DBHelper(this)).getReadableDatabase();
    }

    /**
     * Override onDestroy to close the database.
     */
    
    public void onDestroy() {
    	if( db != null && db.isOpen() ) {
    		db.close();
    	}
    	super.onDestroy();
    }
    
    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
			     
		menu.add(R.string.menuAccounts)
		.setIcon(android.R.drawable.ic_menu_revert)
		.setOnMenuItemClickListener(
			new OnMenuItemClickListener() {
				public boolean onMenuItemClick(final MenuItem item) {
					Intent intent = new Intent(BackupActivity.this, AccountsActivity.class);
					BackupActivity.this.startActivity(intent);    				
					finish();
		            return true;						
				}
			}
		);

		MenuUtil.buildMenu(this, menu);
		
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
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     */

    private void startBackup() {
    	try {
	    	EditText editText = (EditText) findViewById(R.id.name);
	    	String name = editText.getText().toString();
	    	
	    	editText = (EditText) findViewById(R.id.password);
	    	String password = editText.getText().toString();
	    	
	    	new Thread(new Backupper(name, password)).start();
    	} catch(Exception ex) {
            new AlertDialog.Builder(this)
	    		.setTitle("Backup Failed")
	    		.setMessage("The backup can't be started : "+ex.getMessage())
	    		.setIcon(android.R.drawable.ic_dialog_alert)
	    		.setPositiveButton("OK", new OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						BackupActivity.this.finish();
					}
	    		})
	    		.show();		    	
    	}
    }
    
    /**
     * Class to perform the backup
     */
    
    private class Backupper implements Runnable {

    	/**
    	 * The name of the backup.
    	 */
    	private final String name;
    	
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
    	private List<byte[]> waitingBytes = new ArrayList<byte[]>();

    	Backupper(final String name, final String password) 
    		throws NoSuchAlgorithmException, NoSuchPaddingException, 
    		InvalidKeyException, UnsupportedEncodingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
    		this.name = name;
    		cipher = BackupUtils.getCipher(password, Cipher.ENCRYPT_MODE);
    	}
    	
    	/**
    	 * Perform the backup.
    	 */
    	
    	public void run() {
    		updateStatus("Backup Started");
    		try {
    			StringBuilder path = new StringBuilder();  
    			BackupUtils.addBackupPath(path);
    			File backupDir = new File(path.toString());
    			if(!backupDir.exists() && !backupDir.mkdir()) {
    				throw new FileNotFoundException("Unable to create backup directory");
    			}
    			
    			path.append('/');
    			path.append(name);
    			path.append(".fex");
    			
    			final File file = new File(path.toString());
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
    	 * @param cipher The encryption cipher in use.
    	 * @param os The output stream to write to.
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
    	 * @param cipher The encryption cipher in use.
    	 * @param os The output stream to write to.
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
    	 * @param cipher The encryption cipher in use.
    	 * @param os The output stream to write to.
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
    	 * @param table The table to backup.
    	 * @param cipher The encryption cipher in use.
    	 * @param os The output stream to write to.
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
    	 * @param table The table to backup.
    	 * @param cipher The encryption cipher in use.
    	 * @param os The output stream to write to.
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
    	 * @param table The table to backup.
    	 * @param cipher The encryption cipher in use.
    	 * @param os The output stream to write to.
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
    	
    	private String statusUpdate;
    	
    	StatusUpdater(final String statusUpdate) {
    		this.statusUpdate = statusUpdate; 
    	}
    	
    	public void run() {
    		status.setText(statusUpdate);	
    	}
    }
}