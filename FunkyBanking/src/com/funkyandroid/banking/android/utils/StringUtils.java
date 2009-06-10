package com.funkyandroid.banking.android.utils;

public class StringUtils {

	public static boolean isEmpty(final String string) {
		if(string == null || string.length() == 0) {
			return true;
		}
		
		for(int i = 0 ; i < string.length() ; i++) {
			if(!Character.isWhitespace(string.charAt(i))) {
				return false;
			}
		}
		
		return true;
	}
}
