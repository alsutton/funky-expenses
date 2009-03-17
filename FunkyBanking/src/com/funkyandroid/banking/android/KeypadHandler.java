package com.funkyandroid.banking.android;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.funkyandroid.banking.android.expenses.demo.R;

public class KeypadHandler {

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
	
	/**
	 * The context the keypad is being used in.
	 */
	
	private final Context context;
	
	/**
	 * The alert dialog in use.
	 */
	
	private AlertDialog dialog;
	
	/**
	 * The view containing the keypad
	 */
	
	private View keypadView;	

	/**
	 * The edit box being populated
	 */
	
	private EditText editText;

	/**
	 * The id used in the display.
	 */
	
	private int displayId;
	
	/**
	 * The listener for the OK button
	 */
	
	private OnOKListener listener;
	
	public KeypadHandler(final Context context) {		
		super();		
		this.context = context;
	}
	
	public void display(final int id, final int titleResource, final OnOKListener listener) {
		displayId = id;
		this.listener = listener;
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);    	
    	dialog = builder.create();
    	keypadView = dialog.getLayoutInflater().inflate(R.layout.keypad, null);
    	dialog.setView(keypadView);    	
    	dialog.show();
        
    	WindowManager.LayoutParams layout = dialog.getWindow().getAttributes();
    	layout.width = WindowManager.LayoutParams.FILL_PARENT;
    	dialog.getWindow().setAttributes(layout);
		
    	TextView titleView = (TextView) keypadView.findViewById(R.id.title);
    	titleView.setText(titleResource);
    	
    	editText = (EditText) keypadView.findViewById(R.id.typedText);
    	    	
    	KeyPressHandler handler = new KeyPressHandler();
		for(int i = 0  ; i != 26 ; i++) {
			Button button = (Button)keypadView.findViewById(BUTTON_IDS[i]);
			button.setOnClickListener(handler);
		}

		Button button = (Button)keypadView.findViewById(R.id.okButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					dismiss();
        					if(listener != null) {
        						listener.onOK(displayId, editText.getText().toString());
        					}
        				}
        		});
		
    	setToLowerCase();
	}
	
	/**
	 * Dismiss the keypad dialog
	 */
	
	public void dismiss() {
		if( dialog != null && dialog.isShowing() ) {
			dialog.dismiss();
		}
	}
	
	/**
	 * Set the keys to lower case.
	 */
	
	private void setToLowerCase() {
		Button button = (Button)keypadView.findViewById(R.id.buttonSwitch1);
		button.setText("ABC...");
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					KeypadHandler.this.setToUpperCase();
        				}
        		});
		button = (Button)keypadView.findViewById(R.id.buttonSwitch2);
		button.setText("123...");
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					KeypadHandler.this.setToNumerics();
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
		button.setText("abc...");
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					KeypadHandler.this.setToLowerCase();
        				}
        		});
		button = (Button)keypadView.findViewById(R.id.buttonSwitch2);
		button.setText("123...");
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					KeypadHandler.this.setToNumerics();
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
		button.setText("abc...");
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					KeypadHandler.this.setToLowerCase();
        				}
        		});
		button = (Button)keypadView.findViewById(R.id.buttonSwitch2);
		button.setText("ABC...");
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					KeypadHandler.this.setToUpperCase();
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
			Editable currentText = KeypadHandler.this.editText.getText();
			currentText.append(((Button)v).getText());
		}
	}
	
	/**
	 * Interface for classes listening for the OK button.
	 */
	public interface OnOKListener {
		public void onOK(final int id, final String text);
	}
}
