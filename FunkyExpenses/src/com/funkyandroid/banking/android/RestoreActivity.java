package com.funkyandroid.banking.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
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

import com.flurry.android.FlurryAgent;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.adfree.R;
import com.funkyandroid.banking.android.utils.BackupUtils;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class RestoreActivity extends Activity {

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
        super.setTitle(R.string.titleRestore);
        setContentView(R.layout.restore);
        
        ((Button) findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
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
			public void onClick(final View view) {
				startRestore();
			}
        });
    	db = (new DBHelper(this)).getWritableDatabase();
    }

    /**
     * Override onDestroy to close the database.
     */
    
    public void onDestroy() {
    	super.onDestroy();
    	if( db != null && db.isOpen() ) {
    		db.close();
    	}
    }
    

    @Override
    public void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, "8SVYESRG63PTLMNLZPPU");
    }
    
    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
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
					Intent intent = new Intent(RestoreActivity.this, AccountsActivity.class);
					RestoreActivity.this.startActivity(intent);    				
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

    private void startRestore() {
    	try {
	    	EditText editText = (EditText) findViewById(R.id.name);
	    	String name = editText.getText().toString();
	    	
	    	editText = (EditText) findViewById(R.id.password);
	    	String password = editText.getText().toString();
	    	
	    	new Thread(new Restorer(name, password)).start();
    	} catch(Exception ex) {
            new AlertDialog.Builder(this)
	    		.setTitle("Restore Failed")
	    		.setMessage("The restore can't be started : "+ex.getMessage())
	    		.setIcon(android.R.drawable.ic_dialog_alert)
	    		.setPositiveButton("OK", new OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						RestoreActivity.this.finish();
					}
	    		})
	    		.show();		    	
    	}
    }
    
    /**
     * Class to perform the backup
     */
    
    private class Restorer implements Runnable {

    	/**
    	 * The current stream pointer
    	 */
    	
    	private final SteamPointer streamPointer = new SteamPointer();
    	
    	/**
    	 * Four byte buffer used to read data block sizes
    	 */    	
    	private final byte[] fourByteBuffer = new byte[4];

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
    	private FileInputStream fis;

    	Restorer(final String name, final String password) 
    		throws NoSuchAlgorithmException, NoSuchPaddingException, 
    		InvalidKeyException, UnsupportedEncodingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
    		this.name = name;
    		cipher = BackupUtils.getCipher(password, Cipher.DECRYPT_MODE);
    	}
    	
    	/**
    	 * Perform the backup.
    	 */
    	
    	public void run() {
    		updateStatus("Restore Started");
    		try {
    			StringBuilder path = new StringBuilder();  
    			BackupUtils.addBackupPath(path);
    			File backupDir = new File(path.toString());
    			if(!backupDir.exists() ) {
    				throw new FileNotFoundException("Unable to find backup directory.");
    			}
    			
    			path.append('/');
    			path.append(name);
    			path.append(".fex");
    			
    			final File file = new File(path.toString());
    			if(!file.exists() ) {
    				throw new FileNotFoundException("Unable to find backup file.");
    			}
    			
    			fis = new FileInputStream(file);
			    try {
			    	byte[] header = readBlock();
			    	String headerString = getString(header);
			    	if(!BackupUtils.BACKUP_HEADER.equals(headerString)) {
			    		throw new FileNotFoundException("Backup file header is invalid.");
			    	}
			    	
			    	DBHelper.dropTables(db);
			    	DBHelper.createTables(db);
			    	restoreCategories();
			    	restorePayees();
    				restoreSettings();
    				restoreAccounts();
    				restoreEntries();
    	    		updateStatus("Restore Completed.");
    			} finally {
    				fis.close();
    			}
    		} catch(Exception ex) {
    			updateStatus("Error : "+ex.getMessage());
                Log.e("Backup", "Error", ex);
    		}
    	}

    	/**
    	 * Read an encrypted block.
    	 * 
    	 * @throws IOException 
    	 * @throws BadPaddingException 
    	 * @throws IllegalBlockSizeException 
    	 */
    	
    	private byte[] readBlock() throws IOException, IllegalBlockSizeException, BadPaddingException {
    		fis.read(fourByteBuffer);
    		streamPointer.offset = 0;
    		int size = getInt(fourByteBuffer);
    		byte[] data = new byte[size];
    		if(size > 0) {
    			fis.read(data);
    		}
    		streamPointer.offset = 0;
    		return cipher.doFinal(data);
    	}
    	
    	/**
    	 * Decode an int from a buffer
    	 */
    	
    	private int getInt(final byte[] data) {
    		int value = (((int)(data[streamPointer.offset]&0xff))  <<24)+
			       		(((int)(data[streamPointer.offset+1]&0xff))<<16)+
			       		(((int)(data[streamPointer.offset+2]&0xff))<< 8)+
			       		 ((int)(data[streamPointer.offset+3]&0xff));
    		streamPointer.offset += 4;
    		return value;
    	}
    	
    	/**
    	 * Decode an long from a buffer
    	 */
    	
    	private long getLong(final byte[] data) {
    		long value = (((long)(data[streamPointer.offset]&0xff))  <<56)+
			      		 (((long)(data[streamPointer.offset+1]&0xff))<<48)+
			       		 (((long)(data[streamPointer.offset+2]&0xff))<<40)+
			       		 (((long)(data[streamPointer.offset+3]&0xff))<<32)+
			    		 (((long)(data[streamPointer.offset+4]&0xff))<<24)+
			       		 (((long)(data[streamPointer.offset+5]&0xff))<<16)+
			       		 (((long)(data[streamPointer.offset+6]&0xff))<< 8)+
			       		  ((long)(data[streamPointer.offset+7]&0xff));
    		streamPointer.offset += 8;
    		return value;
    	}
    	
    	/**
    	 * Encrypt a string
    	 * @throws UnsupportedEncodingException 
    	 */
    	
    	private String getString(final byte[] data) throws UnsupportedEncodingException {
    		int size = getInt(data);
    		String string = new String(data, streamPointer.offset, size, "UTF-8");
    		streamPointer.offset += size;
    		return string;
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
    	
    	private void restoreCategories() 
    		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Restoring categories....");
    		restoreIDNameTable(DBHelper.CATEGORIES_TABLE_NAME);
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
    	
    	private void restorePayees() 
    		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Restoring payees....");
    		restoreIDNameTable(DBHelper.PAYEE_TABLE_NAME);
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
    	
    	private void restoreIDNameTable(final String table) 
		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		byte[] data = readBlock();
    		int count = getInt(data);
			ContentValues cv = new ContentValues();
    		for(int i = 0 ; i < count ; i++) {
    			data = readBlock();
    			cv.put("_id", getInt(data));
    			cv.put("name", getString(data));
    			db.insert(table, null, cv);
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
    	
    	private void restoreSettings() 
		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Restoring settings....");
    		byte[] data = readBlock();
    		int count = getInt(data);
			ContentValues cv = new ContentValues();
    		for(int i = 0 ; i < count;i++) {
    			data = readBlock();
    			cv.put("name", getString(data));
    			cv.put("value", getString(data));
    			db.insert(DBHelper.SETTINGS_TABLE_NAME, null, cv);
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
    	
    	private void restoreAccounts() 
		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Restoring accounts....");
    		byte[] data = readBlock();
    		int count = getInt(data);
			ContentValues cv = new ContentValues();
    		for(int i = 0 ; i < count;i++) {
    			data = readBlock();
    			cv.put("_id", 				getInt(data));
    			cv.put("opening_balance",	getLong(data));
    			cv.put("balance", 			getLong(data));
    			cv.put("name",				getString(data));
    			cv.put("currency",			getString(data));
    			db.insert(DBHelper.ACCOUNTS_TABLE_NAME, null, cv);
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
    	
    	private void restoreEntries() 
		throws IllegalBlockSizeException, BadPaddingException, IOException {
    		updateStatus("Restoring entries....");
    		byte[] data = readBlock();
    		int count = getInt(data);
			ContentValues cv = new ContentValues();
    		for(int i = 0 ; i < count;i++) {
    			data = readBlock();
    			cv.put("_id", 			getInt(data));
    			cv.put("account_id",	getInt(data));
    			cv.put("category_id",	getInt(data));
    			cv.put("payee_id",		getInt(data));
    			cv.put("link_id",		getInt(data));
    			cv.put("timestamp",		getLong(data));
    			cv.put("type",			getInt(data));
    			cv.put("amount",		getLong(data));
    			db.insert(DBHelper.ENTRIES_TABLE_NAME, null, cv);
    		}
    	}
    	
    	private class SteamPointer {
    		int offset = 0;
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