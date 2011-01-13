package com.funkyandroid.banking.android.expenses.adfree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.SettingsManager;
import com.funkyandroid.banking.android.utils.BackupUtils;

public class MyBackupAgent extends BackupAgent {
	/**
	 * Columns in an id -> name mapping table.
	 */
	private final static String[] NAME_ID_COLS = { "_id", "name" };

	private final static String[] SETTINGS_COLS = { "name", "value" };

	private final static String[] ACCOUNTS_COLS = { "_id", "opening_balance", "balance", "name", "currency" };

	private final static String[] ENTRIES_COLS = { "_id", "account_id", "category_id", "payee_id", "link_id", "timestamp", "type", "amount" };

	/**
	 * Lock object to prevent a simultaneous back-up and restore.
	 */
	private static final Object DATA_LOCK = new Object();
	
	/**
	 * The backup format for this back.
	 */
	
	private static final int BACKUP_FORMAT_VERSION = 1;
	

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		SQLiteDatabase db = (new DBHelper(this)).getWritableDatabase();
		try {
			byte[] header = createStateHeader(db);

			boolean performBackup;
			if(oldState == null) {
				performBackup = true;
			} else {
				performBackup = checkBackupState(db, oldState);
			}
			
			if(!performBackup) {
				return;
			}
			
            // Write the format details
            data.writeEntityHeader("HEADER", 12);
            data.writeEntityData(header, 12);
            
            new MyBackupMaker(data, db).run();
	            
    		synchronized(DATA_LOCK) {
	            final DataOutputStream out = new DataOutputStream(new FileOutputStream(newState.getFileDescriptor()));
	            try {
	            	out.write(header);
	            } finally {
	            	out.close();
	            }
    		}
		} catch(Exception ex) {
            Log.e("Funky Expenses", "Backup Error", ex);
		} finally {
			db.close();
		}
	}

	/**
	 * Handle a restore request.
	 */
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		synchronized(DATA_LOCK) {
			SQLiteDatabase db = (new DBHelper(this)).getWritableDatabase();
			try {
				while(data.readNextHeader()) {
					try {
						String key = data.getKey();
						int underscoreIdx = key.indexOf('_');
						if(underscoreIdx == -1) {
							Log.e("FunkyExpenses", "Invalid backup key "+key);
							continue;
						}
						
						final String table = key.substring(0, underscoreIdx);
						final String id = key.substring(underscoreIdx+1);
						
						if(			DBHelper.CATEGORIES_TABLE_NAME.equals(table)
						|| 			DBHelper.PAYEE_TABLE_NAME.equals(table)) {
							restoreIdNameTableEntry(db, table, id, data);
						} else if ( DBHelper.SETTINGS_TABLE_NAME.equals(table) ) {
							restoreSettingsEntry(db, id, data);
						} else if ( DBHelper.ACCOUNTS_TABLE_NAME.equals(table) ) {
							restoreAccountsEntry(db, id, data);
						} else if ( DBHelper.ENTRIES_TABLE_NAME.equals(table) ) {
							restoreEntriesEntry(db, id, data);
						} else {
							Log.e("FunkyExpenses", "Unable to determine restore location for "+key);
						}
					} catch(Exception ex) {
						Log.e("FunkyExpenses", "Exception during restore", ex);
					}
				} 
	    		synchronized(DATA_LOCK) {
		            final DataOutputStream out = new DataOutputStream(new FileOutputStream(newState.getFileDescriptor()));
		            try {
		            	out.write(createStateHeader(db));
		            } finally {
		            	out.close();
		            }
	    		}
			} finally {
				db.close();
			}
		}
	}

	/**
	 * Create the state header
	 */
	
	private byte[] createStateHeader(final SQLiteDatabase db) {
		final byte[] header = new byte[12];
		BackupUtils.serialize(BACKUP_FORMAT_VERSION, header, 0);
        final String lastUpdateString = SettingsManager.get(db, "LAST_UPDATE");
        if(lastUpdateString == null) {
			BackupUtils.serialize(0L, header, 4);
        } else {
        	BackupUtils.serialize(Long.parseLong(lastUpdateString), header, 4);
        }
        return header;
	}
	
	/**
	 * Checks the data version from the old backup to see if a backup is needed.
	 */
	
	private boolean checkBackupState(final SQLiteDatabase db, final ParcelFileDescriptor oldState) {
		synchronized(DATA_LOCK) {
	        try {
	            final DataInputStream in = new DataInputStream(new FileInputStream(oldState.getFileDescriptor()));
	            
	            // The backup version must match.
	            final int backupFormatVersion = in.readInt();
	            if(backupFormatVersion != BACKUP_FORMAT_VERSION) {
	            	return true;
	            }
	            
	            // Check if there are any data changes since the last backup
	            final String lastUpdateString = SettingsManager.get(db, "LAST_UPDATE");
	            long lastUpdate;
	            if(lastUpdateString == null) {
	            	lastUpdate = 0;
	            } else {
	            	lastUpdate = Long.parseLong(lastUpdateString);
	            }
	            
	            return lastUpdate != in.readLong();
	        } catch( Exception ex ) {
	        	return true;
	        }
		}
	}
	
	/**
	 * Read a byte array from the restore stream
	 * @throws IOException 
	 */
	
	private byte[] readBytesToRestore(final BackupDataInput data) throws IOException {
		byte[] bytes = new byte[data.getDataSize()];
		int offset = 0;
		while(true) {
			int bytesRead = data.readEntityData(bytes, offset, bytes.length-offset);
			if(bytesRead == 0) {
				return bytes;
			}
			offset += bytesRead;
		}		
	}
	
	/**
	 * Restore some entries into an ID/Name mapping table
	 * @throws IOException 
	 */
	
	final void restoreIdNameTableEntry(final SQLiteDatabase db, final String tableName, 
			final String idString, BackupDataInput data) throws IOException {
		String name = new String(readBytesToRestore(data), "UTF-8");
		
		ContentValues values = new ContentValues();
		values.put("name", name);
		
		final String[] whereArgs = { idString };
		if( db.update(tableName, values, "_id = ?", whereArgs) == 0 ) {
			values.put("_id", idString);
			db.insert(tableName, null, values);
		}
		Log.i("FunkyExpenses", values.toString());
	}
	
	/**
	 * Restore an entry from the settings table
	 * @throws IOException 
	 */
	
	private void restoreSettingsEntry(final SQLiteDatabase db, final String idString, 
			final BackupDataInput data) throws IOException {
		String value = new String(readBytesToRestore(data), "UTF-8");
		
		ContentValues values = new ContentValues();
		values.put("value", value);
		
		final String[] whereArgs = { idString };
		if( db.update(DBHelper.SETTINGS_TABLE_NAME, values, "name = ?", whereArgs) == 0 ) {
			values.put("name", idString);
			db.insert(DBHelper.SETTINGS_TABLE_NAME, null, values);
		}		
		Log.i("FunkyExpenses", values.toString());
	}
	
	/**
	 * Restore an accounts entry.
	 * 
	 * @param db
	 * @param idString
	 * @param data
	 * @throws IOException
	 */
	private void restoreAccountsEntry(final SQLiteDatabase db, final String idString, 
			final BackupDataInput data) throws IOException {
		final byte[] restoreData = readBytesToRestore(data);
		
		ContentValues values = new ContentValues();
		values.put("opening_balance", BackupUtils.getLong(restoreData, 0));
		values.put("balance", BackupUtils.getLong(restoreData, 8));
		int nameLength = BackupUtils.getInt(restoreData, 16);
		values.put("name", BackupUtils.getString(restoreData, 20, nameLength));
		int currencyLength = BackupUtils.getInt(restoreData, 20+nameLength);
		values.put("currency", BackupUtils.getString(restoreData, 24+nameLength, currencyLength));
		
		final String[] whereArgs = { idString };
		if( db.update(DBHelper.ACCOUNTS_TABLE_NAME, values, "_id = ?", whereArgs) == 0 ) {
			values.put("_id", idString);
			db.insert(DBHelper.ACCOUNTS_TABLE_NAME, null, values);
		}		
		Log.i("FunkyExpenses", values.toString());
	}

	private void restoreEntriesEntry(final SQLiteDatabase db, final String idString, 
			final BackupDataInput data) 
		throws IOException {
		final byte[] restoreData = readBytesToRestore(data);

		ContentValues values = new ContentValues();
		values.put("account_id", BackupUtils.getInt(restoreData, 0));
		values.put("category_id", BackupUtils.getInt(restoreData, 4));
		values.put("payee_id", BackupUtils.getInt(restoreData, 8));
		values.put("link_id", BackupUtils.getInt(restoreData, 12));
		values.put("timestamp", BackupUtils.getLong(restoreData, 16));
		values.put("type", BackupUtils.getInt(restoreData, 24));
		values.put("amount", BackupUtils.getLong(restoreData, 28));

		final String[] whereArgs = { idString };
		if( db.update(DBHelper.ENTRIES_TABLE_NAME, values, "_id = ?", whereArgs) == 0 ) {
			values.put("_id", idString);
			db.insert(DBHelper.ENTRIES_TABLE_NAME, null, values);
		}		
		Log.i("FunkyExpenses", values.toString());
	}

	
	/**
     * Class to perform the backup
     */
    
    private class MyBackupMaker implements Runnable {

    	/**
    	 * The list of bytes waiting to be encrypted.
    	 */
    	private List<byte[]> waitingBytes = new ArrayList<byte[]>();

    	/**
    	 * The data backup transport
    	 */
    	
    	private BackupDataOutput data;
    	
    	/**
    	 * The database accessor
    	 */
    	
    	private SQLiteDatabase db;
    	
    	MyBackupMaker(final BackupDataOutput data, final SQLiteDatabase db) {
    		this.data = data;
    		this.db = db;
    	}
    	
    	/**
    	 * Perform the backup.
    	 */
    	
    	public void run() {
    		try {
				backupCategories();
				backupPayees();
				backupSettings();
				backupAccounts();
				backupEntries();
    		} catch(Exception ex) {
                Log.e("Funky Expenses", "Error", ex);
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
    	
    	private void writeData(final String header) 
    		throws IOException {
    		  	
    		int totalLength = 0;
    		for( byte[] array : waitingBytes ) {
    			totalLength += array.length;
    		}
    		
    		data.writeEntityHeader(header, totalLength);
    		for( byte[] array : waitingBytes ) {
    			data.writeEntityData(array, array.length);
    		}
    		
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
    		Cursor cursor = db.query( table, NAME_ID_COLS, null, null, null, null, null );
    		try {
    			while(cursor.moveToNext()) {
    				if(cursor.isNull(1)) {
    					continue;
    				}

    				StringBuilder header = new StringBuilder(table);
    				header.append('_');
    				header.append(cursor.getInt(0));
    				
    				byte[] name = cursor.getString(1).getBytes("UTF-8");
    				data.writeEntityHeader(header.toString(), name.length);
    				data.writeEntityData(name, name.length);
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
    		Cursor cursor = db.query( DBHelper.SETTINGS_TABLE_NAME, SETTINGS_COLS, null, null, null, null, null);
    		try {
    			while(cursor.moveToNext()) {
    				if( cursor.isNull(0) || cursor.isNull(1)) {
    					continue;
    				}
    				    				
    				add(cursor.getString(1));
    				writeData("setting_"+cursor.getString(0));
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
    		Cursor cursor = db.query( DBHelper.ACCOUNTS_TABLE_NAME, ACCOUNTS_COLS, null, null, null, null, null);
    		try {
    			while(cursor.moveToNext()) {
    				int id = cursor.getInt(0);
    				add(cursor.getLong(1));
    				add(cursor.getLong(2));
    				add(cursor.getString(3));
    				add(cursor.getString(4));
    				writeData("account_"+id);
    			}
    			Log.i("FunkyExpenses", "BUP "+cursor.getString(3));
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
    		Cursor cursor = db.query( DBHelper.ENTRIES_TABLE_NAME,  ENTRIES_COLS, null, null, null, null, null);
    		try {
    			while(cursor.moveToNext()) {
    				int id = cursor.getInt(0);
    				add(cursor.getInt(1));
    				add(cursor.getInt(2));
    				add(cursor.getInt(3));
    				add(cursor.getInt(4));
    				add(cursor.getLong(5));
    				add(cursor.getInt(6));
    				add(cursor.getLong(7));
					writeData("entry_"+id);
    			}
    		} finally {
    			cursor.close();
    		}
    	}
    }
}
