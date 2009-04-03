package com.funkyandroid.banking.android.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class AboutUtil {

	/**
	 * Private constructor to avoid instantiation.
	 */
	
	private AboutUtil() {
		super();
	}
	
	/**
	 * Show the about dialogue
	 * 
	 * @param activity The activity that wants to show the about box.
	 */
	
	public static final void showDialog(final Context context) {
		PackageInfo pi = null;
		try {
			pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// Ignore not found exceptions
		}
		StringBuilder message = new StringBuilder(64);
		if( pi != null ) {
			message.append("Version ");
			message.append(pi.versionName);
			message.append("\n\n");
		}
		message.append("Copyright 2009 Funky Android Limited, All Rights Reserved.\n\nSee www.funkyandroid.com for more information.");
        new AlertDialog.Builder(context).setTitle(pi.applicationInfo.labelRes)
	            .setMessage(message.toString())
	            .setPositiveButton("OK", null)
	            .show();		
	}
}
