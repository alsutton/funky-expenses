package com.funkyandroid.banking.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.utils.MenuUtil;

public class EditAccountActivity extends Activity {

	/**
	 * The list of currencies available on this system.
	 */
	
	private String[] currencies;
	
	/**
	 * The transaction being edited.
	 */
	
	private Account account;

	/**
	 * Was the account fetched from the database.
	 */
	
	private boolean fetched;

	/**
	 * The button holding the account name
	 */
	
	private Button nameButton;
	
	/**
	 * The button holding the account name
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
        super.setTitle(R.string.accountTitleText);
        setContentView(R.layout.edit_account);

        
		Button button = (Button) findViewById(R.id.cancelButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					if( fetched ) {        				    	
        				    	SQLiteDatabase db = (new DBHelper(EditAccountActivity.this)).getWritableDatabase();
        						try {    	
       					    		AccountManager.delete(db, account);	    		
        						} finally {
        							db.close();
        						}
        					}
        					EditAccountActivity.this.finish();
        				}
        		});
		button = (Button) findViewById(R.id.okButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					storeAccountDetails();
        					EditAccountActivity.this.finish();
        				}
        		});
		button = (Button) findViewById(R.id.currency);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					editCurrency();
        				}
        		});
        
        nameButton = (Button) findViewById(R.id.accountName);
        nameButton.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					EditAccountActivity.this.editName();
        				}
        		});
		
        amountButton = (Button) findViewById(R.id.amount);
        amountButton.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					EditAccountActivity.this.editAmount();
        				}
        		});
        
        new Thread(new PostCreateThread()).start();
    }
        
    /**
     * Get the account details when started. 
     */
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	fetched = false;
    	
    	Intent startingIntent = getIntent();    	
    	
    	int accountId = startingIntent.getIntExtra("com.funkyandroid.banking.account_id", -1);    	
    	if( accountId == -1 ) {
    		createEmptyAccount();
    		return;
    	}
    	
    	SQLiteDatabase db = (new DBHelper(this)).getReadableDatabase();
		try {    	
			account = AccountManager.getById(db, accountId);
		} finally {
			db.close();
		}

    	if( account == null ) {
    		createEmptyAccount();
    		return;
    	} else {	    	
	    	fetched = true;	    	
	    	
    		nameButton.setText(account.getName());

    		long amount = account.getOpeningBalance();
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
	    	
    	updateCurrencyInformation(account.getCurrency());
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
     * Creates a new empty account
     */
    
    private void createEmptyAccount() {
		account = new Account();
    	Currency currency = Currency.getInstance(Locale.getDefault());
		account.setCurrency(currency.getCurrencyCode());
    	updateCurrencyInformation(account.getCurrency());    	
		amountButton.setText("0.00");
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
     * Store the account details into the database.
     */
    
    public void storeAccountDetails() {
    	account.setName(nameButton.getText().toString());

    	long oldOpeningBalance = account.getOpeningBalance();    	

    	long openingBalance = 0;
    	String amountText = amountButton.getText().toString();
    	int dotIdx = amountText.indexOf('.');
    	String amountString = amountText.substring(0, dotIdx);
    	if(amountString != null && amountString.length() > 0) {
    		openingBalance += Long.parseLong(amountString) * 100;
    	}
    	amountString = amountText.substring(dotIdx+1);
    	if(amountString != null && amountString.length() > 0) {
    		openingBalance += Long.parseLong(amountString);
    	}
    	account.setOpeningBalance(openingBalance);
    	
    	SQLiteDatabase db = (new DBHelper(this)).getWritableDatabase();
		try {    	
		   	if( fetched ) {
		   		AccountManager.update(db, account, oldOpeningBalance);
	    	} else {
	    		AccountManager.create(db, account);	    		
	    	}
		} finally {
			db.close();
		}
    }
    

    /**
     * Edit the payee.
     */
    
    private void editName() {
    	synchronized(this) {
    		if(kh == null) {
    			kh = new KeypadHandler(this);
    		}
    	}
    	
    	kh.display(-1, R.string.entryAmountText, nameButton.getText(),
    			new KeypadHandler.OnOKListener() {
    		public void onOK(final int id, final String text) {
    			nameButton.setText(text);
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
     * Edit the currency the account is in.
     */
    
    private void editCurrency() {    	
        new AlertDialog.Builder(this)
			.setTitle("Choose a Currency")
			.setItems(currencies, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	updateCurrencyInformation(currencies[which]);
	            }
	        })
	        .create()
	        .show();
    }
    
    /**
     * Update the account currency
     */
    
    private void updateCurrencyInformation(final String currencyCode) {
    	account.setCurrency(currencyCode);
    	Currency currency = Currency.getInstance(account.getCurrency());
    	
    	TextView textView = (TextView) findViewById(R.id.currencySymbol);
    	textView.setText(currency.getSymbol());

    	Button currencyButton = (Button) findViewById(R.id.currency);
    	currencyButton.setText(currency.getCurrencyCode());   	
    }
    
    
    /**
     * A post onCreate initialisation thread.
     */
    
    private class PostCreateThread implements Runnable {
    	public void run() {            
            
            List<String> currencyList = new ArrayList<String>(); 
            for(Locale locale : Locale.getAvailableLocales()) {
            	try {
    	        	Currency currency = Currency.getInstance(locale);
    	        	if(currency == null) {
    	        		continue;
    	        	}
    	        	String code = currency.getCurrencyCode();
    	        	if( ! currencyList.contains(code) ) {
    	        		currencyList.add(code);
    	        	}
            	} catch(IllegalArgumentException iae) {
            		; // Do nothing. This is thrown for unsupported locales.
            	}
            }
            Collections.sort(currencyList);
            currencies = new String[currencyList.size()];
            currencyList.toArray(currencies);
    	}
    }
}