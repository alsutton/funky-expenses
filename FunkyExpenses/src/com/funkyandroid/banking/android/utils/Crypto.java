package com.funkyandroid.banking.android.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto {

	public static String getHash(final String data) 
		throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if( data == null || data.length() == 0 ) {
			return null;
		}
		
		MessageDigest md = MessageDigest.getInstance("SHA-1");

		md.update(data.getBytes("UTF8"));
		byte[] hash = md.digest();
		
		StringBuilder hashStringBuilder = new StringBuilder(hash.length*2);
		for(byte thisByte : hash) {
			if(thisByte < 0) {
				hashStringBuilder.append('0');
			}
			hashStringBuilder.append(Integer.toHexString(Byte.valueOf(thisByte).intValue()));
		}
		
		return hashStringBuilder.toString();
	}
}
