package com.funkyandroid.banking.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import com.funkyandroid.banking.android.data.CategoryManager;

/**
 * Suggestions adapter for categories.
 */

public class CategorySuggestionsAdapter extends SimpleCursorAdapter {

	/**
	 * The database to use.
	 */

	private final SQLiteDatabase db;

	public CategorySuggestionsAdapter(final Context context, final int layout, final Cursor c,
			final String[] from, final int[] to, final SQLiteDatabase db) {
		super(context, layout, c, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		this.db = db;
	}

	@Override
	public String convertToString(final Cursor cursor)
	{
		return cursor.getString(0);
	}

	@Override
	public Cursor runQueryOnBackgroundThread(final CharSequence constraint)
	{
		if(db == null || !db.isOpen() || constraint == null || constraint.length() == 0) {
			return super.runQueryOnBackgroundThread(constraint);
		}
		return CategoryManager.getMatchesFor(db, constraint.toString());
	}
}
