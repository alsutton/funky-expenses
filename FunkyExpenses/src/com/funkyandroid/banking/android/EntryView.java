package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.funkyandroid.banking.android.utils.BalanceFormatter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EntryView extends LinearLayout {
	/**
	 * The date of the transaction
	 */

	private TextView date;
	
	/**
	 * The icon image.
	 */
	
	private TextView name;
	
	/**
	 * The application details.
	 */
	
	private TextView value;

	/**
	 * Constructor.
	 * 
	 * @param context The context the view is in.
	 */
	public EntryView(final Context context, final Cursor cursor, 
			final String currencySymbol) {
		super(context);
		
		setOrientation(LinearLayout.VERTICAL);

		setPadding(getPaddingLeft(), getPaddingTop()+2, getPaddingRight()+5, getPaddingBottom()+2);

		LinearLayout dateAndValue = new LinearLayout(context);
		dateAndValue.setOrientation(LinearLayout.HORIZONTAL);
		
		ViewGroup.LayoutParams dateLayout = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		date = new TextView(context);
		date.setLayoutParams(dateLayout);
		date.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		dateAndValue.addView(date);		
		
		ViewGroup.LayoutParams valueLayout = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		value = new TextView(context);
		value.setLayoutParams(valueLayout);
		value.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		value.setTypeface(date.getTypeface(), Typeface.BOLD);
		dateAndValue.addView(value);
		
		addView(dateAndValue);
		
		ViewGroup.LayoutParams nameLayout = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		name = new TextView(context);
		name.setLayoutParams(nameLayout);
		name.setGravity(Gravity.CENTER_VERTICAL);
		name.setTypeface(date.getTypeface(), Typeface.ITALIC);
		addView(name);
		
		updateData(cursor, currencySymbol);
	}
	
	/**
	 * Set the icon.
	 */
	
	public void updateData(final Cursor cursor, final String currencySymbol) {		
		this.name.setText(cursor.getString(1));

		long balance = cursor.getLong(2);
		if			( balance < 0 ) {
			value.setTextColor(Color.rgb(0xc0, 0x00, 0x00));
		} else if	( balance > 0 ) {
			value.setTextColor(Color.rgb(0x00, 0xc0, 0x00));
		} else {
			value.setTextColor(Color.rgb(0xcf, 0xc0, 0x00));
		}
		
		StringBuilder valueString = new StringBuilder(10);
		BalanceFormatter.format(valueString, balance, currencySymbol);
		value.setText(valueString.toString());

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
		Date entryDate = new Date(cursor.getLong(3));
		date.setText(sdf.format(entryDate));
	}	
}
