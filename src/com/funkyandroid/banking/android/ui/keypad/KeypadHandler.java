package com.funkyandroid.banking.android.ui.keypad;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.funkyandroid.banking.android.expenses.demo.R;

public class KeypadHandler {

	/**
	 * The context the keypad is being used in.
	 */

	protected final Context context;

	/**
	 * The alert dialog in use.
	 */

	protected AlertDialog dialog;

	/**
	 * The view containing the keypad
	 */

	protected View keypadView;

	/**
	 * The edit box being populated
	 */

	protected EditText editText;

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

	/**
	 * Display a keypad to the user.
	 *
	 * @param id An ID to identify events from the requested keypad.
	 * @param titleResource The string resource for the title.
	 * @param startText The initial text for the box.
	 * @param listener A listener for the OK button.
	 * @param keypadLayout The ID of the layout resource holdinf the keypad layout.
	 */

	public void display(final int id, final int titleResource,
			final CharSequence startText, final OnOKListener listener,
			final boolean hiddenEntry) {
		int keypadLayout = hiddenEntry ? R.layout.cupcake_password_keypad : R.layout.cupcake_keypad;

		displayId = id;
		this.listener = listener;

    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	dialog = builder.create();
    	keypadView = dialog.getLayoutInflater().inflate(keypadLayout, null);
    	dialog.setView(keypadView);
    	dialog.show();

    	WindowManager.LayoutParams layout = dialog.getWindow().getAttributes();
    	layout.width = WindowManager.LayoutParams.MATCH_PARENT;
    	dialog.getWindow().setAttributes(layout);

    	TextView titleView = (TextView) keypadView.findViewById(R.id.title);
    	titleView.setText(titleResource);

    	editText = (EditText) keypadView.findViewById(R.id.typedText);
    	editText.setText(startText);

		Button button = (Button)keypadView.findViewById(R.id.okButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				@Override
						public void onClick(final View view) {
        					dismiss();
        					notifyListener();
        				}
        		});
        editText.requestFocus();
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
	 * Notify the listener that OK has been pressed.
	 */

	private void notifyListener() {
		if(listener != null) {
			listener.onOK(displayId, editText.getText().toString());
		}
	}

	/**
	 * Interface for classes listening for the OK button.
	 */
	public interface OnOKListener {
		public void onOK(final int id, final String text);
	}
}
