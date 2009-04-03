package com.funkyandroid.banking.android.ui.keypad;

import android.content.Context;

import android.os.Build.VERSION;

/**
 * Factory to determine and return the correct keypad type for the
 * version of Android the app is being run on.
 * 
 * @author Al Sutton
 */
public final class KeypadFactory {
	
	/**
	 * Get the correct keypad handler for this OS.
	 * 
	 * @param context The context in which the application is running.
	 */
	
	public static final KeypadHandler getKeypadHandler(final Context context) {
		if(VERSION.RELEASE.equals("1")){
			return new PreCupcakeKeypadHandler(context);
		}
		if(VERSION.SDK.equals("2")){
			return new PreCupcakeKeypadHandler(context);
		}
		
		return new CupcakeKeypadHandler(context);
	}
}
