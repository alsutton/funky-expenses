package com.funkyandroid.banking.android.utils;

import android.content.Context;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
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