package com.funkyandroid.banking.android;

import java.util.Currency;

import com.funkyandroid.banking.android.utils.BalanceFormatter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AccountView extends LinearLayout {

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
	public AccountView(Context context) {
		super(context);
		
		setOrientation(LinearLayout.VERTICAL);

		setPadding(getPaddingLeft(), getPaddingTop()+2, getPaddingRight(), getPaddingBottom()+2);
		
		ViewGroup.LayoutParams nameLayout = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		name = new TextView(context);
		name.setLayoutParams(nameLayout);
		name.setGravity(Gravity.CENTER_VERTICAL);
		name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		addView(name);

		ViewGroup.LayoutParams valueLayout = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		value = new TextView(context);
		value.setLayoutParams(valueLayout);
		value.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		value.setTypeface(value.getTypeface(), Typeface.BOLD);
		value.setPadding(value.getPaddingLeft()+10, value.getPaddingTop(), value.getPaddingRight()+10, value.getPaddingBottom());
		addView(value);
	}
	
	/**
	 * Set the name of the account
	 */
	
	public void setNameText(final String name) {
		this.name.setText(name);
	}
	
	/**
	 * Set the icon.
	 */
	
	public void setBalance(final long balance, final String currencyCode) {				
		if			( balance < 0 ) {
			value.setTextColor(Color.rgb(0xc0, 0x00, 0x00));
		} else if	( balance > 0 ) {
			value.setTextColor(Color.rgb(0x00, 0xc0, 0x00));
		} else {
			value.setTextColor(Color.rgb(0xcf, 0xc0, 0x00));
		}
				
		StringBuilder valueString = new StringBuilder(10);
		valueString.append("Balance : ");
		BalanceFormatter.format(valueString, balance, Currency.getInstance(currencyCode));
		valueString.append(' ');
		value.setText(valueString.toString());
	}	
}
