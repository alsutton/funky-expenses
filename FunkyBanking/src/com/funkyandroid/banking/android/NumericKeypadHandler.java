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

public class NumericKeypadHandler {

	/**
	 * The IDs for the buttons.
	 */
	
	private static final int[] BUTTON_IDS = {
		R.id.button1,  R.id.button2,  R.id.button3,  R.id.button4,  R.id.button5,
		R.id.button6,  R.id.button7,  R.id.button8,  R.id.button9,  R.id.button0,
		R.id.buttonDot
	};
	
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
	
	public NumericKeypadHandler(final Context context) {		
		super();		
		this.context = context;
	}
	
	public void display(final int id, final int titleResource,
			final CharSequence startText, final OnOKListener listener) {
		displayId = id;
		this.listener = listener;
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);    	
    	dialog = builder.create();
    	keypadView = dialog.getLayoutInflater().inflate(R.layout.numerickeypad, null);
    	dialog.setView(keypadView);    	
    	dialog.show();
        
    	WindowManager.LayoutParams layout = dialog.getWindow().getAttributes();
    	layout.width = WindowManager.LayoutParams.FILL_PARENT;
    	dialog.getWindow().setAttributes(layout);
		
    	TextView titleView = (TextView) keypadView.findViewById(R.id.title);
    	titleView.setText(titleResource);
    	
    	editText = (EditText) keypadView.findViewById(R.id.typedText);
    	editText.setText(startText);
    	    	
    	KeyPressHandler handler = new KeyPressHandler();
		for(int i = 0  ; i != 11 ; i++) {
			Button button = (Button)keypadView.findViewById(BUTTON_IDS[i]);
			button.setOnClickListener(handler);
		}

		
		Button button = (Button)keypadView.findViewById(R.id.okButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					dismiss();
        					notifyListener();
        				}
        		});
        
		button = (Button)keypadView.findViewById(R.id.delButton);
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
	 * The handler for all keypresses
	 */
	
	private class KeyPressHandler implements OnClickListener {
		public void onClick(final View v) {
			Editable currentText = NumericKeypadHandler.this.editText.getText();
			
			int position = NumericKeypadHandler.this.editText.getSelectionStart();
			
			String newText = ((Button)v).getText().toString();
			String currentTextString = currentText.toString();
			int dotIdx = currentTextString.indexOf('.');
			if( dotIdx > -1 ) {
				if( newText.equals(".")) {
					return;
				}
				if( dotIdx < currentText.length() - 2 && position > dotIdx) {
					return;
				}
			}
			
			currentText.insert(position, newText);
		}
	}

	/**
	 * Notify the listener that OK has been pressed.
	 */
	
	private void notifyListener() {
		if(listener != null) {
			String text = editText.getText().toString();
			
			int dotIdx = text.indexOf('.');			
			if( dotIdx == text.length()-3 && dotIdx > 0) {
				listener.onOK(displayId, text);
				return;
			}
			
			StringBuilder builder = new StringBuilder(text.length()+3);
			if( dotIdx == 0 ) {
				builder.append('0');
			}
			if( dotIdx == -1 ) {
				builder.append(text);
				builder.append(".00");
			} else if ( dotIdx == text.length()-1 ) {
				builder.append(text);
				builder.append("00");    				    					
			} else if ( dotIdx == text.length()-2) {
				builder.append(text);
				builder.append('0');    				    					
			} else {
				builder.append(text);
			}
			
			listener.onOK(displayId, builder.toString());
		}
	}
	
	/**
	 * Interface for classes listening for the OK button.
	 */
	public interface OnOKListener {
		public void onOK(final int id, final String text);
	}
}
