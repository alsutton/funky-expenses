package com.funkyandroid.banking.android.utils;

import android.content.Context;
import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import com.funkyandroid.banking.android.Preferences;
import com.funkyandroid.banking.android.expenses.demo.R;

public class MenuUtil {

	/**
	 * Private constructor to avoid instantiation.
	 */

	private MenuUtil() {
		super();
	}

	/**
	 *
	 */

	public static void buildMenu(final Context context, final Menu menu) {
		menu.add(R.string.titlePreferences)
		.setIcon(android.R.drawable.ic_menu_preferences)
		.setOnMenuItemClickListener(
			new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(final MenuItem item) {
					context.startActivity(new Intent(context, Preferences.class));
		            return true;
				}
			}
		);
		menu.add(R.string.titleAbout)
		.setIcon(android.R.drawable.ic_menu_info_details)
		.setOnMenuItemClickListener(
			new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(final MenuItem item) {
		    		AboutUtil.showDialog(context);
		            return true;
				}
			}
		);
	}
}
