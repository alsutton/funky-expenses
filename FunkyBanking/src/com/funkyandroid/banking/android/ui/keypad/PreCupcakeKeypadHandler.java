package com.funkyandroid.banking.android.ui.keypad;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.funkyandroid.banking.android.expenses.adfree.R;

public final class PreCupcakeKeypadHandler 
	extends KeypadHandler {

	/**
	 * The IDs for the buttons.
	 */
	
	private static final int[] BUTTON_IDS = {
		R.id.button1,  R.id.button2,  R.id.button3,  R.id.button4,  R.id.button5,
		R.id.button6,  R.id.button7,  R.id.button8,  R.id.button9,  R.id.button10,
		R.id.button11, R.id.button12, R.id.button13, R.id.button14, R.id.button15,
		R.id.button16, R.id.button17, R.id.button18, R.id.button19, R.id.button20,
		R.id.button21, R.id.button22, R.id.button23, R.id.button24, R.id.button25,
		R.id.button26
	};
	
	/**
	 * The keys for the lower case keypad
	 */
	
	private static final String[] LOWERCASE_CHARS = 
		{ "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
		  "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
	
	/**
	 * The keys for the lower case keypad
	 */
	
	private static final String[] UPPERCASE_CHARS = 
		{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
		  "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
	
	/**
	 * The keys for the lower case keypad
	 */
	
	private static final String[] NUMERIC_CHARS = 
		{ "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", " ", "!", "\"",
		  "%", "^", "&", "*", "(", ")", "[", "]", "{", "}", ".", ",", "/" };
	
	
	public PreCupcakeKeypadHandler(final Context context) {		
		super( context );		
	}
	
	@Override
	public void display(final int id, final int titleResource, 
			final CharSequence startText, final OnOKListener listener,
			final boolean hiddenEntry) {

		int keypadLayout = hiddenEntry ? R.layout.password_keypad : R.layout.keypad;
		super.display(id, titleResource, startText, listener, keypadLayout);

		KeyPressHandler handler = new KeyPressHandler();
		for(int i = 0  ; i != 26 ; i++) {
			Button button = (Button)keypadView.findViewById(BUTTON_IDS[i]);
			button.setOnClickListener(handler);
		}

		Button button = (Button)keypadView.findViewById(R.id.delButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					Editable text = editText.getText();
        					if(text == null || text.length() == 0) {
        						return;
        					}
        					
        					int selStart = editText.getSelectionStart();
        					int selEnd = editText.getSelectionEnd();
        					
        					if( selStart != selEnd ) {
            					text.delete(selStart, selEnd);        						
        					} else if( selStart > 0 ) {
    							text.delete(selStart-1, selStart);        							
        					}
        					
        				}
        		});
		
    	setToLowerCase();
	}
	
	/**
	 * Set the keys to lower case.
	 */
	
	private void setToLowerCase() {
		Button button = (Button)keypadView.findViewById(R.id.buttonSwitch1);
		button.setText(R.string.keypadSwitchUpper);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					PreCupcakeKeypadHandler.this.setToUpperCase();
        				}
        		});
		button = (Button)keypadView.findViewById(R.id.buttonSwitch2);
		button.setText(R.string.keypadSwitchNumbers);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					PreCupcakeKeypadHandler.this.setToNumerics();
        				}
        		});


		for(int i = 0  ; i != 26 ; i++) {
			button = (Button)keypadView.findViewById(BUTTON_IDS[i]);
			button.setText(LOWERCASE_CHARS[i]);
		}
	}
	
	/**
	 * Set the keys to lower case.
	 */
	
	private void setToUpperCase() {
		Button button = (Button)keypadView.findViewById(R.id.buttonSwitch1);
		button.setText(R.string.keypadSwitchLower);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					PreCupcakeKeypadHandler.this.setToLowerCase();
        				}
        		});
		button = (Button)keypadView.findViewById(R.id.buttonSwitch2);
		button.setText(R.string.keypadSwitchNumbers);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					PreCupcakeKeypadHandler.this.setToNumerics();
        				}
        		});


		for(int i = 0  ; i != 26 ; i++) {
			button = (Button)keypadView.findViewById(BUTTON_IDS[i]);
			button.setText(UPPERCASE_CHARS[i]);
		}
	}
	
	/**
	 * Set the keys to lower case.
	 */
	
	private void setToNumerics() {
		Button button = (Button)keypadView.findViewById(R.id.buttonSwitch1);
		button.setText(R.string.keypadSwitchLower);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					PreCupcakeKeypadHandler.this.setToLowerCase();
        				}
        		});
		button = (Button)keypadView.findViewById(R.id.buttonSwitch2);
		button.setText(R.string.keypadSwitchUpper);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					PreCupcakeKeypadHandler.this.setToUpperCase();
        				}
        		});


		for(int i = 0  ; i != 26 ; i++) {
			button = (Button)keypadView.findViewById(BUTTON_IDS[i]);
			button.setText(NUMERIC_CHARS[i]);
		}
	}
	
	/**
	 * The handler for all keypresses
	 */
	
	private class KeyPressHandler implements OnClickListener {
		public void onClick(final View v) {
			Editable currentText = PreCupcakeKeypadHandler.this.editText.getText();
			int position = PreCupcakeKeypadHandler.this.editText.getSelectionStart();
			currentText.insert(position, ((Button)v).getText());
		}
	}
}
