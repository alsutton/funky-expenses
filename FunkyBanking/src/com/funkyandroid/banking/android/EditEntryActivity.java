package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.Transaction;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class EditEntryActivity extends Activity {
	
	/**
	 * The transaction being edited.
	 */
	
	private Transaction transaction;
	
	/**
	 * Whether or not the transaction was fetched from the database.
	 */
	
	private boolean fetched = false;
	
	/**
	 * The payee button.
	 */
	
	private Button payeeButton;
	
	/**
	 * The amount button.
	 */
	
	private Button amountButton;
		
	/**
	 * The text keypad handler.
	 */
	
	private KeypadHandler kh;
	
	/**
	 * The amount keypad helper.
	 */
	
	private NumericKeypadHandler nkh;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle(R.string.entryTitleText);
        setContentView(R.layout.edit_entry);

		Button button = (Button) findViewById(R.id.newEntryDateButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        		        	Calendar cal = Calendar.getInstance();
        		        	cal.setTime(new Date(EditEntryActivity.this.transaction.getTimestamp()));
        		        	
        		            new DatePickerDialog(
        		            		EditEntryActivity.this, 
        		            		new DateListener(), 
        		            		cal.get(Calendar.YEAR),
        		                    cal.get(Calendar.MONTH), 
        		                    cal.get(Calendar.DAY_OF_MONTH)).show();
        				}
        		});
        
		button = (Button) findViewById(R.id.okButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					storeEntryDetails();
        					EditEntryActivity.this.finish();
        				}
        		});
        
		button = (Button) findViewById(R.id.cancelButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					if( fetched ) {        				    	
        				    	SQLiteDatabase db = (new DBHelper(EditEntryActivity.this)).getWritableDatabase();
        						try {    	
       					    		TransactionManager.delete(db, transaction);	    		
        						} finally {
        							db.close();
        						}
        					}
        					EditEntryActivity.this.finish();
        				}
        		});
        
        amountButton = (Button) findViewById(R.id.amount);
        amountButton.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					EditEntryActivity.this.editAmount();
        				}
        		});

		payeeButton = (Button) findViewById(R.id.payee);
		payeeButton.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					EditEntryActivity.this.editPayee();
        				}
        		});
    }
        
    /**
     * Get the account details when started. 
     */
    
    @Override
    public void onStart() {
    	super.onStart();
    	Intent startingIntent = getIntent();    	
    	

    	String currencySymbol = startingIntent.getStringExtra(
    				"com.funkyandroid.banking.account_currency"
    			);
    	TextView cSymb = (TextView) findViewById(R.id.currencySymbol);
    	cSymb.setText(currencySymbol);
    	int transactionId = startingIntent.getIntExtra("com.funkyandroid.banking.transaction_id", -1);
    	if( transactionId == -1 ) {
	    	int accountId = startingIntent.getIntExtra("com.funkyandroid.banking.account_id", -1);    	
	    	if( accountId == -1 ) {		    	
	    		finish();
	    		return;
	    	}
	    	
    		transaction = new Transaction();
    		transaction.setAccountId(accountId);
    		transaction.setTimestamp(System.currentTimeMillis());

    		amountButton.setText("0.00");
    		((RadioButton) findViewById(R.id.debitButton)).setSelected(true);
			
    		fetched = false;
    	} else {	
	    	SQLiteDatabase db = (new DBHelper(this)).getReadableDatabase();
			try {    	
				transaction = TransactionManager.getById(db, transactionId);
		    	if( transaction == null ) {
		    		finish();
		    		return;    		
		    	}
			} finally {
				db.close();
			}
			fetched = true;
			
 			switch(transaction.getType()) {
				case	Transaction.TYPE_CREDIT:
				{
					RadioButton button = (RadioButton) findViewById(R.id.creditButton);
					button.setChecked(true);
					break;
				}
				case	Transaction.TYPE_DEBIT:
				{
					RadioButton button = (RadioButton) findViewById(R.id.debitButton);
					button.setChecked(true);
					break;
				}
			}

 			payeeButton.setText(transaction.getPayee());
 			
 			long amount = transaction.getAmount();
 			if( amount < 0 ) {
 				amount = 0 - amount;
 			}

 			StringBuilder builder = new StringBuilder(16);
 			builder.append(Long.toString(amount/100));
 			builder.append('.');
 			long remainder = amount%100;
 			if(remainder < 10) {
 				builder.append('0');
 			}
 			builder.append(remainder);
 			amountButton.setText(builder.toString());
 	    	
 	    	Button button = (Button) findViewById(R.id.okButton);
 	    	button.setText(R.string.updateButtonText);
 	    	button = (Button) findViewById(R.id.cancelButton);
 	    	button.setText(R.string.deleteButtonText);
 	    	
    	}
		
    	updateDate();
    }

    /**
     * Check that the keypads have been disposed of.
     */
    
    @Override
    public void onDestroy() {
    	if( nkh != null ) {
    		nkh.dismiss();
    	}
    	if( kh != null ) {
    		kh.dismiss();
    	}
    	super.onDestroy();
    }
    
    /**
     * Set up the menu for the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
			     
		MenuUtil.buildMenu(this, menu);
		
		return true;
	}
    

    /**
     * Update the date button with the latest value. 
     */

    private void updateDate() {
    	Button button = (Button) findViewById(R.id.newEntryDateButton);
    	SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM, yyyy");
    	button.setText(sdf.format(new Date(transaction.getTimestamp())));
    }

    /**
     * Listener for when the user finishes setting the date.
     */
    private class DateListener implements OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int month, int day) {
        	Calendar cal = Calendar.getInstance();
        	cal.set(year, month, day);
        	EditEntryActivity.this.transaction.setTimestamp(cal.getTime().getTime());
        	EditEntryActivity.this.updateDate();
        }
    }

    /**
     * Edit the payee.
     */
    
    private void editPayee() {
    	synchronized(this) {
    		if(kh == null) {
    			kh = new KeypadHandler(this);
    		}
    	}
    	
    	kh.display(-1, R.string.entryAmountText, payeeButton.getText(),
    			new KeypadHandler.OnOKListener() {
    		public void onOK(final int id, final String text) {
    			payeeButton.setText(text);
    		}    		
    	});
    }

    /**
     * Edit the amount.
     */
    
    private void editAmount() {
    	synchronized(this) {
    		if(nkh == null) {
    			nkh = new NumericKeypadHandler(this);
    		}
    	}
    	
    	nkh.display(-1, R.string.entryAmountText, amountButton.getText(),
    			new NumericKeypadHandler.OnOKListener() {
    		public void onOK(final int id, final String text) {
				amountButton.setText(text);
    		}    		
    	});
    }

    /**
     * Store the account details into the database.
     */
    
    public void storeEntryDetails() {
    	transaction.setPayee(payeeButton.getText().toString());

    	RadioGroup transactionType = (RadioGroup) findViewById(R.id.type);
    	
    	int type;
    	switch (transactionType.getCheckedRadioButtonId()) {
    		case R.id.debitButton:
    			type = Transaction.TYPE_DEBIT;
    			break;
    		case R.id.creditButton:
				type = Transaction.TYPE_CREDIT;
				break;
			default:
				throw new RuntimeException("Unknown button ID"+transactionType.getCheckedRadioButtonId());
    	}
    	transaction.setType(type);

    	long oldAmount = transaction.getAmount();
    	long amount = 0;
    	String amountText = amountButton.getText().toString();
    	int dotIdx = amountText.indexOf('.');
    	String amountString = amountText.substring(0, dotIdx);
    	if(amountString != null && amountString.length() > 0) {
    		amount += Long.parseLong(amountString) * 100;
    	}
    	amountString = amountText.substring(dotIdx+1);
    	if(amountString != null && amountString.length() > 0) {
        	amount += Long.parseLong(amountString);
    	}
    	if( type == Transaction.TYPE_DEBIT ) {
    		amount = 0 - amount;
    	}
    	transaction.setAmount(amount);
    	
    	
    	SQLiteDatabase db = (new DBHelper(this)).getWritableDatabase();
		try {    	
		   	if( fetched ) {
		   		TransactionManager.update(db, transaction, oldAmount);
	    	} else {
	    		TransactionManager.create(db, transaction);	    		
	    	}
		} finally {
			db.close();
		}
    }
}