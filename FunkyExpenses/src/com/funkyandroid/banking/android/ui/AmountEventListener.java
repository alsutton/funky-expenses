package com.funkyandroid.banking.android.ui;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class AmountEventListener implements OnFocusChangeListener {

	/**
	 * The old listener
	 */

	private final OnFocusChangeListener oldListener;

	/**
	 * Constructor. Stores old listener.
	 */

	public AmountEventListener( final OnFocusChangeListener oldListener ) {
		this.oldListener = oldListener;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		EditText thisView = (EditText)v;
		String text = thisView.getText().toString();
		if( hasFocus ) {
			if( text != null && text.length() > 0 && onlyContainsZeros(text)) {
				thisView.setText("");
			}
		} else {
			if( text == null || text.length() == 0 || onlyContainsZeros(text)) {
				final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());
				thisView.setText("0"+dfs.getDecimalSeparator()+"00");
			}
		}

		if(oldListener != null) {
			oldListener.onFocusChange(v, hasFocus);
		}
	}

	/**
	 * Method to see if a string only contains zeros
	 */

	private boolean onlyContainsZeros(final String string) {
		for(char c : string.toCharArray()) {
			if(Character.isDigit(c) && c != '0') {
				return false;
			}
		}

		return true;
	}
}
