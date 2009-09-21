package com.funkyandroid.banking.android.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class MinorAmountEventListener implements OnFocusChangeListener, TextWatcher {

	/**
	 * The old listener
	 */
	
	private OnFocusChangeListener oldListener;
	
	/**
	 * Constructor. Stores old listener.
	 */
	
	public MinorAmountEventListener( final OnFocusChangeListener oldListener ) {
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
				thisView.setText("00");
			}					
		}
		
		if(oldListener != null) {
			oldListener.onFocusChange(v, hasFocus);
		}
	}

    public void afterTextChanged(Editable s)
    {
    	if(s.length() > 2) {
    		s.delete(2, s.length());
    	}
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
    }	
}
