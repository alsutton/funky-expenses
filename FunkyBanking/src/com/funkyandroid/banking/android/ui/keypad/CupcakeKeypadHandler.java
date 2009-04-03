package com.funkyandroid.banking.android.ui.keypad;

import android.content.Context;

import com.funkyandroid.banking.android.KeypadHandler;
import com.funkyandroid.banking.android.expenses.demo.R;

public class CupcakeKeypadHandler 
	extends KeypadHandler {

	public CupcakeKeypadHandler(final Context context) {		
		super(context);		
	}
	
	@Override
	public void display(final int id, final int titleResource, 
			final CharSequence startText, final OnOKListener listener,
			final boolean hiddenEntry) {
		
		int keypadLayout = hiddenEntry ? R.layout.cupcake_password_keypad : R.layout.cupcake_keypad;
		super.display(id, titleResource, startText, listener, keypadLayout);
	}	
}
