package com.funkyandroid.banking.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.utils.BackupUtils;

public class RestoreService extends IntentService {

    /**
     * The tag for logs.
     */

    private static final String LOG_TAG = "FE-Restore";

	/**
	 * The bundle parameter for the backup name
	 */

	public static final String BACKUP_NAME_BUNDLE_PARAM = "name";

	/**
	 * The bundle parameter for the backup password
	 */

	public static final String BACKUP_PASSWORD_BUNDLE_PARAM = "password";

	/**
	 * The broadcast indicating there is a status update.
	 */

	public final static String PROGRESS_UPDATE_BROADCAST = "com.funkyandroid.banking.RESTORE_PROGRESS_UPDATE";

	/**
	 * The broadcast indicating the restore completed.
	 */

	public final static String PROGRESS_COMPLETE_BROADCAST = "com.funkyandroid.banking.RESTORE_PROGRESS_COMPLETE";

	/**
	 * Four byte buffer used to read data block sizes
	 */
	private final byte[] fourByteBuffer = new byte[4];

	/**
	 * Pass-through constructor
	 */
	public RestoreService() {
		super("FunkyExpenses Restore Service");
	}

	/**
	 * Perform the backup.
	 */

	@Override
	public void onHandleIntent(final Intent intent) {
		final String name = intent.getExtras().getString(RestoreService.BACKUP_NAME_BUNDLE_PARAM);
		final String password = intent.getExtras().getString(RestoreService.BACKUP_PASSWORD_BUNDLE_PARAM);
		final Cipher cipher;
		try {
			cipher = BackupUtils.getCipher(password, Cipher.DECRYPT_MODE);
		} catch(Exception ex) {
			publishException(ex);
			return;
		}


		publishProgress(0);
		final SQLiteDatabase db = (new DBHelper(this)).getWritableDatabase();
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

			final StreamPointer streamPointer = new StreamPointer();
			final FileInputStream fis = new FileInputStream(file);
		    try {
		    	byte[] header = readBlock(fis, streamPointer, cipher);
		    	String headerString = getString(streamPointer, header);
		    	if(!BackupUtils.BACKUP_HEADER.equals(headerString)) {
		    		throw new FileNotFoundException("Backup file header is invalid.");
		    	}

		    	DBHelper.dropTables(db);
		    	DBHelper.createTables(db);
		    	restoreCategories(fis, streamPointer, cipher, db);
	    		publishProgress(20);
		    	restorePayees(fis, streamPointer, cipher, db);
	    		publishProgress(40);
				restoreSettings(fis, streamPointer, cipher, db);
	    		publishProgress(60);
				restoreAccounts(fis, streamPointer, cipher, db);
	    		publishProgress(80);
				restoreEntries(fis, streamPointer, cipher, db);
	    		publishProgress(100);
	    		publishComplete();
			} finally {
				fis.close();
			}
		} catch(Exception ex) {
			publishException(ex);
		} finally {
			db.close();
		}
	}

	/**
	 * Publishes the progress by sending out a broadcast intent.
	 */

	public void publishProgress(final int progress) {
		Intent progressIntent = new Intent(RestoreService.PROGRESS_UPDATE_BROADCAST);
		progressIntent.putExtra("progress", progress);
		sendBroadcast(progressIntent);
	}

	/**
	 * Publishes the completion of the restore by sending out a broadcast intent.
	 */

	public void publishComplete() {
		Intent progressIntent = new Intent(RestoreService.PROGRESS_COMPLETE_BROADCAST);
		sendBroadcast(progressIntent);
	}

	/**
	 * Publish an exception to all listeners
	 */

	public void publishException(final Exception ex) {
		Intent progressIntent = new Intent(RestoreService.PROGRESS_UPDATE_BROADCAST);
		progressIntent.putExtra("error", ex.getMessage());
		sendBroadcast(progressIntent);
        Log.e(LOG_TAG, "Error during restore.", ex);
	}

	/**
	 * Read an encrypted block.
	 *
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */

	private byte[] readBlock(final FileInputStream fis, final StreamPointer streamPointer, final Cipher cipher)
		throws IOException, IllegalBlockSizeException, BadPaddingException {

		int size;
		synchronized(fourByteBuffer) {
			fis.read(fourByteBuffer);
			size = getInt(fourByteBuffer);
		}

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

	private int getInt(final StreamPointer streamPointer, final byte[] data) {
		int value = (((data[streamPointer.offset]&0xff))  <<24)+
		       		(((data[streamPointer.offset+1]&0xff))<<16)+
		       		(((data[streamPointer.offset+2]&0xff))<< 8)+
		       		 ((data[streamPointer.offset+3]&0xff));
		streamPointer.offset += 4;
		return value;
	}

	/**
	 * Decode an int from a buffer
	 */

	private int getInt(final byte[] data) {
		return (((data[0]&0xff))  <<24)+
                (((data[1]&0xff))<<16)+
                (((data[2]&0xff))<< 8)+
                ((data[3]&0xff));
	}

	/**
	 * Decode an long from a buffer
	 */

	private long getLong(final StreamPointer streamPointer, final byte[] data) {
		long value = (((long)(data[streamPointer.offset]&0xff))  <<56)+
		      		 (((long)(data[streamPointer.offset+1]&0xff))<<48)+
		       		 (((long)(data[streamPointer.offset+2]&0xff))<<40)+
		       		 (((long)(data[streamPointer.offset+3]&0xff))<<32)+
		    		 (((long)(data[streamPointer.offset+4]&0xff))<<24)+
		       		 (((long)(data[streamPointer.offset+5]&0xff))<<16)+
		       		 (((long)(data[streamPointer.offset+6]&0xff))<< 8)+
		       		  ((data[streamPointer.offset+7]&0xff));
		streamPointer.offset += 8;
		return value;
	}

	/**
	 * Encrypt a string
	 * @throws UnsupportedEncodingException
	 */

	private String getString(final StreamPointer streamPointer, final byte[] data) throws UnsupportedEncodingException {
		int size = getInt(streamPointer, data);
		String string = new String(data, streamPointer.offset, size, "UTF-8");
		streamPointer.offset += size;
		return string;
	}

	/**
	 * Restore the categories
	 *
     * @param fis The Input stream being restored from.
     * @param streamPointer Information about the stream being restored from.
	 * @param cipher The encryption cipher in use.
     * @param db The database connection to restore to.
	 *
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */

	private void restoreCategories(final FileInputStream fis, final StreamPointer streamPointer, final Cipher cipher,
			final SQLiteDatabase db)
		throws IllegalBlockSizeException, BadPaddingException, IOException {
		restoreIDNameTable(fis, streamPointer, cipher, db, DBHelper.CATEGORIES_TABLE_NAME);
	}

	/**
	 * Backup the payees
	 *
     * @param fis The Input stream being restored from.
     * @param streamPointer Information about the stream being restored from.
     * @param cipher The encryption cipher in use.
     * @param db The database connection to restore to.
	 *
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */

	private void restorePayees(final FileInputStream fis, final StreamPointer streamPointer, final Cipher cipher,
			final SQLiteDatabase db)
		throws IllegalBlockSizeException, BadPaddingException, IOException {
		restoreIDNameTable(fis, streamPointer, cipher, db, DBHelper.PAYEE_TABLE_NAME);
	}

	/**
	 * Backup a table with an id and name in it.
	 *
     * @param fis The Input stream being restored from.
     * @param streamPointer Information about the stream being restored from.
     * @param cipher The encryption cipher in use.
     * @param db The database connection to restore to.
	 *
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */

	private void restoreIDNameTable(final FileInputStream fis, final StreamPointer streamPointer, final Cipher cipher,
			final SQLiteDatabase db, final String table)
	throws IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] data = readBlock(fis, streamPointer, cipher);
		int count = getInt(streamPointer, data);
		ContentValues cv = new ContentValues();
		for(int i = 0 ; i < count ; i++) {
			data = readBlock(fis, streamPointer, cipher);
			cv.put("_id", getInt(streamPointer, data));
			cv.put("name", getString(streamPointer, data));
			db.insert(table, null, cv);
		}
	}

	/**
	 * Backup a table with an id and name in it.
	 *
     * @param fis The Input stream being restored from.
     * @param streamPointer Information about the stream being restored from.
     * @param cipher The encryption cipher in use.
     * @param db The database connection to restore to.
	 *
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */

	private void restoreSettings(final FileInputStream fis, final StreamPointer streamPointer, final Cipher cipher,
			final SQLiteDatabase db)
	throws IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] data = readBlock(fis, streamPointer, cipher);
		int count = getInt(streamPointer, data);
		ContentValues cv = new ContentValues();
		for(int i = 0 ; i < count;i++) {
			data = readBlock(fis, streamPointer, cipher);
			cv.put("name", getString(streamPointer, data));
			cv.put("value", getString(streamPointer, data));
			db.insert(DBHelper.SETTINGS_TABLE_NAME, null, cv);
		}
	}

	/**
	 * Backup a table with an id and name in it.
	 *
     * @param fis The Input stream being restored from.
     * @param streamPointer Information about the stream being restored from.
     * @param cipher The encryption cipher in use.
     * @param db The database connection to restore to.
	 *
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */

	private void restoreAccounts(final FileInputStream fis, final StreamPointer streamPointer, final Cipher cipher,
			final SQLiteDatabase db)
	throws IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] data = readBlock(fis, streamPointer, cipher);
		int count = getInt(streamPointer, data);
		ContentValues cv = new ContentValues();
		for(int i = 0 ; i < count;i++) {
			data = readBlock(fis, streamPointer, cipher);
			cv.put("_id", 				getInt(streamPointer, data));
			cv.put("opening_balance",	getLong(streamPointer, data));
			cv.put("balance", 			getLong(streamPointer, data));
			cv.put("name",				getString(streamPointer, data));
			cv.put("currency",			getString(streamPointer, data));
			db.insert(DBHelper.ACCOUNTS_TABLE_NAME, null, cv);
		}
	}

	/**
	 * Backup a table with an id and name in it.
	 *
     * @param fis The Input stream being restored from.
     * @param streamPointer Information about the stream being restored from.
     * @param cipher The encryption cipher in use.
     * @param db The database connection to restore to.
	 *
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */

	private void restoreEntries(final FileInputStream fis, final StreamPointer streamPointer, final Cipher cipher,
			final SQLiteDatabase db)
	throws IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] data = readBlock(fis, streamPointer, cipher);
		int count = getInt(streamPointer, data);
		ContentValues cv = new ContentValues();
		for(int i = 0 ; i < count;i++) {
			data = readBlock(fis, streamPointer, cipher);
			cv.put("_id", 			getInt(streamPointer, data));
			cv.put("account_id",	getInt(streamPointer, data));
			cv.put("category_id",	getInt(streamPointer, data));
			cv.put("payee_id",		getInt(streamPointer, data));
			cv.put("link_id",		getInt(streamPointer, data));
			cv.put("timestamp",		getLong(streamPointer, data));
			cv.put("type",			getInt(streamPointer, data));
			cv.put("amount",		getLong(streamPointer, data));
			db.insert(DBHelper.ENTRIES_TABLE_NAME, null, cv);
		}
	}

	/**
	 * Container for holding the current pointer in te stream being read.
	 */
	private class StreamPointer {
		int offset = 0;
	}

}
