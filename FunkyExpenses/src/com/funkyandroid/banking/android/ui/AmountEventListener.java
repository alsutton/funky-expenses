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

	public void onFocusChange(View v, boolean hasFocus) {
		EditText thisView = (EditText)v;
		String text = thisView.getText().toString();
		if( hasFocus ) {
			if( text != null && text.length() > 0 && Double.parseDouble(text) == 0) {
				thisView.setText("");
			}
		} else {
			if( text == null || text.length() == 0 || Double.parseDouble(text) == 0) {
				final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());
				thisView.setText("0"+dfs.getDecimalSeparator()+"00");
			}
		}

		if(oldListener != null) {
			oldListener.onFocusChange(v, hasFocus);
		}
	}
}
