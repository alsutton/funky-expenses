package com.funkyandroid.banking.android.ui;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class MajorAmountEventListener implements OnFocusChangeListener {

	/**
	 * The old listener
	 */
	
	private OnFocusChangeListener oldListener;
	
	/**
	 * Constructor. Stores old listener.
	 */
	
	public MajorAmountEventListener( final OnFocusChangeListener oldListener ) {
		this.oldListener = oldListener;
	}
	
	public void onFocusChange(View v, boolean hasFocus) {
		EditText thisView = (EditText)v;
		String text = thisView.getText().toString();
		if( hasFocus ) {		
			if( text != null && text.length() > 0 && Integer.parseInt(text) == 0) {
				thisView.setText("");
			}
		} else {
			if( text == null || text.length() == 0) {
				thisView.setText("0");
			}					
		}
		
		if(oldListener != null) {
			oldListener.onFocusChange(v, hasFocus);
		}
	}
}
