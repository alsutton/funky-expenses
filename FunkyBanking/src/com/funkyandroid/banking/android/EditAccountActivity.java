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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.ui.MajorAmountEventListener;
import com.funkyandroid.banking.android.ui.MinorAmountEventListener;
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
        
        EditText editText = (EditText) findViewById(R.id.amountMinor);
        MinorAmountEventListener minorAmountEventListener = 
        	new MinorAmountEventListener(editText.getOnFocusChangeListener());
        editText.addTextChangedListener(minorAmountEventListener);
        editText.setOnFocusChangeListener(minorAmountEventListener);
        
        editText = (EditText) findViewById(R.id.amountMajor);
        MajorAmountEventListener majorAmountEventListener = 
        	new MajorAmountEventListener(editText.getOnFocusChangeListener());
        editText.setOnFocusChangeListener(majorAmountEventListener);
        
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
        	} catch(NullPointerException npe) {
        		; // Do nothing. Locale does not have a currency code.
        	}
        }
        Collections.sort(currencyList);
        currencies = new String[currencyList.size()];
        currencyList.toArray(currencies);
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
    		EditText editText = (EditText) findViewById(R.id.amountMajor);
	    	editText.setText("0");	    	
	    	editText = (EditText) findViewById(R.id.amountMinor);
	    	editText.setText("00");    	
    		return;
    	} else {	    	
	    	fetched = true;	    	
	    	EditText editText = (EditText) findViewById(R.id.accountName);
	    	editText.setText(account.getName());	    	
	    	editText = (EditText) findViewById(R.id.amountMajor);
	    	editText.setText(Long.toString(account.getOpeningBalance()/100));	    	
	    	editText = (EditText) findViewById(R.id.amountMinor);
	    	StringBuilder value = new StringBuilder(2);
	    	long amountMinor = account.getOpeningBalance()%100;
	    	if(amountMinor < 10) {
	    		value.append('0');
	    	}
	    	value.append(amountMinor);
	    	editText.setText(value.toString());    	

	    	Button button = (Button) findViewById(R.id.okButton);
 	    	button.setText(R.string.updateButtonText);
 	    	button = (Button) findViewById(R.id.cancelButton);
 	    	button.setText(R.string.deleteButtonText);
    	}
	    	
    	updateCurrencyInformation(account.getCurrency());
    }

    /**
     * Creates a new empty account
     */
    
    private void createEmptyAccount() {
		account = new Account();
		Currency currency;
		try {
			currency = Currency.getInstance(Locale.getDefault());
			account.setCurrency(currency.getCurrencyCode());
		} catch(IllegalArgumentException iae) {
			account.setCurrency("EUR");
		}
    	updateCurrencyInformation(account.getCurrency());    	
    }
    
    /**
     * Store the account details.
     */
    
    
    /**
     * Check to see if the . key has been pressed, if so move to the minor currency area
     */
    
    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    	if( event.getKeyCode() == KeyEvent.KEYCODE_PERIOD ) {
    		EditText minor = ((EditText)findViewById(R.id.amountMinor));
    		if(Integer.parseInt(minor.getText().toString()) == 0) {
    			minor.setText("");
    		}
    		minor.requestFocus();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
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
    	EditText editText = (EditText) findViewById(R.id.accountName);
    	account.setName(editText.getText().toString());

    	long oldOpeningBalance = account.getOpeningBalance();
    	
    	long openingBalance = 0;
    	editText = (EditText) findViewById(R.id.amountMajor);
    	String major = editText.getText().toString();
    	if( major != null && major.length() > 0 ) {
    		openingBalance += Long.parseLong(major) * 100;
    	}
    	
    	editText = (EditText) findViewById(R.id.amountMinor);
    	String minor = editText.getText().toString();
    	if( minor != null && minor.length() > 0 ) {
        	openingBalance += Long.parseLong(minor);    	
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
    
}