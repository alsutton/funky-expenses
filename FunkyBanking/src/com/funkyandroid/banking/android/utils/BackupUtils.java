package com.funkyandroid.banking.android.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import android.os.Environment;

public final class BackupUtils {

	/**
	 * The initialisation vector for the CBC encryption.
	 */
	
	public static final byte[] IV = new byte[8];
	
	/**
	 * The header to identify a backup.
	 */
	
	public static final String BACKUP_HEADER = "FUNKEXPN";
	
	/**
	 * Private constructor to avoid instantiation.
	 */
	
	private BackupUtils() {
		super();
	}
	
	/**
	 * Add the backup path to a StringBuilder.
	 * 
	 * @param pathBuilder The Builder to get the path to.
	 * 
	 * @throws IOException Thrown if there is a problem building the path.
	 */
	
	public static void addBackupPath(final StringBuilder pathBuilder) throws IOException {
		pathBuilder.append(Environment.getExternalStorageDirectory().getCanonicalPath());
		pathBuilder.append("/FunkyExpensesBackups");		
	}
	
	/**
	 * Serialise an int to a byte array.
	 * 
	 * @param data The data to serialise
	 * @param array The array to serialise to.
	 * @param pos The position to serialise to.
	 */
	
	public static void serialize(final int data, final byte[] array, final int position) {
		array[position] = (byte)((data & 0xFF000000) >> 24);
		array[position+1] = (byte)((data & 0xFF0000) >> 16);
		array[position+2] = (byte)((data & 0xFF00) >> 8);
		array[position+3] = (byte)((data & 0xFF));
	}	
	
	/**
	 * Puts some data into a binary array
	 * 
	 * @param data The data to serialise
	 * @param array The array to serialise to.
	 * @param pos The position to serialise to.
	 */
	
	public static void serialize(final long data, final byte[] array, final int position) {
		array[position] =   (byte)((data & 0xFF00000000000000L) >> 56);
		array[position+1] = (byte)((data & 0xFF000000000000L) >> 48);
		array[position+2] = (byte)((data & 0xFF0000000000L) >> 40);
		array[position+3] = (byte)((data & 0xFF00000000L) >> 32);
		array[position+4] = (byte)((data & 0xFF000000) >> 24);
		array[position+5] = (byte)((data & 0xFF0000) >> 16);
		array[position+6] = (byte)((data & 0xFF00) >> 8);
		array[position+7] = (byte)(data & 0xFF);
	}	
	
	/**
	 * Get the cipher
	 * 
	 * @param mode The mode the cipher should be started in.
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchPaddingException 
	 */
	
	public static Cipher getCipher(final String password, final int mode) 
		throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, 
		InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchPaddingException {
		MessageDigest digester = MessageDigest.getInstance("MD5");
		byte[] digestBytes = digester.digest(password.getBytes("UTF-8"));
		
		byte[] keyBytes = new byte[32];
		System.arraycopy(digestBytes, 0, keyBytes,  0, 16);
		System.arraycopy(digestBytes, 0, keyBytes, 16, 16);
		
        DESedeKeySpec desedeKeySpec = new DESedeKeySpec(keyBytes);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("DESede");
        SecretKey key = keyFac.generateSecret(desedeKeySpec);     		
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding"); 
		cipher.init(mode, key, new IvParameterSpec(BackupUtils.IV));
		return cipher;
	}
}
