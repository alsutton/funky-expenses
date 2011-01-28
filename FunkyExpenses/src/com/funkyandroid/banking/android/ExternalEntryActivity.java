package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.funkyandroid.banking.android.data.Account;
import com.funkyandroid.banking.android.data.AccountManager;
import com.funkyandroid.banking.android.data.CategoryManager;
import com.funkyandroid.banking.android.data.CurrencyManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.PayeeManager;
import com.funkyandroid.banking.android.data.Transaction;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.ui.MajorAmountEventListener;
import com.funkyandroid.banking.android.ui.MinorAmountEventListener;
import com.funkyandroid.banking.android.utils.MenuUtil;
import com.funkyandroid.banking.android.utils.StringUtils;

public class ExternalEntryActivity extends Activity {

	/**
	 * Shared empty String.
	 */

	private static final String EMPTY_STRING = "";

	/**
	 * The transaction being edited.
	 */

	private Transaction transaction;

	/**
	 * The Database connection
	 */

	private SQLiteDatabase db;

	/**
	 * The accounts spinner.
	 */

	private Spinner accountsSpinner;

	/**
	 * The category suggestor.
	 */

	private CategorySuggestionsAdapter categorySuggester;

	/**
	 * The category suggestor.
	 */

	private PayeeSuggestionsAdapter payeeSuggester;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle(R.string.entryTitleText);
        setContentView(R.layout.new_external_entry);

		Button button = (Button) findViewById(R.id.newEntryDateButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        		        	Calendar cal = Calendar.getInstance();
        		        	cal.setTime(new Date(ExternalEntryActivity.this.transaction.getTimestamp()));

        		            new DatePickerDialog(
        		            		ExternalEntryActivity.this,
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
        					ExternalEntryActivity.this.finish();
        				}
        		});

		button = (Button) findViewById(R.id.cancelButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				public void onClick(final View view) {
        					ExternalEntryActivity.this.finish();
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

    	db = (new DBHelper(this)).getWritableDatabase();

    	categorySuggester =
    		new CategorySuggestionsAdapter(
					this,
					android.R.layout.simple_dropdown_item_1line,
					null,
					CategoryManager.NAME_COL,
					new int[] {android.R.id.text1},
					db);
    	AutoCompleteTextView categoryEntry = (AutoCompleteTextView) findViewById(R.id.category);
    	categoryEntry.setAdapter(categorySuggester);

    	payeeSuggester =
    		new PayeeSuggestionsAdapter(
					this,
					android.R.layout.simple_dropdown_item_1line,
					null,
					PayeeManager.NAME_COL,
					new int[] {android.R.id.text1},
					db );
    	AutoCompleteTextView payeeEntry = (AutoCompleteTextView) findViewById(R.id.payee);
    	payeeEntry.setAdapter(payeeSuggester);

    }

    /**
     * Override onDestroy to close the database.
     */

    @Override
	public void onDestroy() {
    	super.onDestroy();
    	if( db != null && db.isOpen() ) {
    		db.close();
    	}
    }

    /**
     * Get the account details when started.
     */

    @Override
    public void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, "8SVYESRG63PTLMNLZPPU");
    	Intent startingIntent = getIntent();

    	((TextView) findViewById(R.id.currencySymbol)).setText("");

		transaction = new Transaction();
		transaction.setAccountId(-1);

		long date = startingIntent.getLongExtra("com.funkyandroid.DATE", -1);
		if( date == -1) {
			date = System.currentTimeMillis();
		}
		transaction.setTimestamp(date);
    	updateDate();

		String payee = startingIntent.getStringExtra("com.funkyandroid.PAYEE");
		if( payee == null || payee.length() == 0 ) {
			((TextView)findViewById(R.id.payee)).setText(EMPTY_STRING);
		} else {
			((TextView)findViewById(R.id.payee)).setText(payee);
		}


		String category = startingIntent.getStringExtra("com.funkyandroid.CATEGORY");
		if( category == null || category.length() == 0 ) {
			((TextView)findViewById(R.id.category)).setText(EMPTY_STRING);
		} else {
			((TextView)findViewById(R.id.category)).setText(category);

		}

		((TextView)findViewById(R.id.amountMajor)).setText("0");
		((TextView)findViewById(R.id.amountMinor)).setText("00");
		String amount = startingIntent.getStringExtra("com.funkyandroid.AMOUNT");
		if( amount != null && amount.length() > 0 ) {
			int sepIdx = amount.indexOf('.');
			if(sepIdx == -1) {
				((TextView)findViewById(R.id.amountMajor)).setText(amount);
			} else {
				((TextView)findViewById(R.id.amountMajor)).setText(amount.substring(0, sepIdx));
				if(sepIdx < amount.length()) {
					((TextView)findViewById(R.id.amountMinor)).setText(amount.substring(sepIdx+1));
				}
			}
		}

		RadioButton button = (RadioButton) findViewById(R.id.debitButton);
		button.setSelected(true);

		Cursor cursor = AccountManager.getAll(db);
		startManagingCursor(cursor);
    	AccountsAdapter adapter = new AccountsAdapter(
					this,
					android.R.layout.simple_spinner_item,
					cursor,
					AccountManager.NAME_COL,
					new int[] {android.R.id.text1} );
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	accountsSpinner = (Spinner)findViewById(R.id.accountSpinner);
    	accountsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(final AdapterView<?> adapterView, final View view,
					final int position, final long id) {
				int realId = (int)(id & 0xffffffff);
				Account theAccount = AccountManager.getById(db, realId);
				if( theAccount != null ) {
					ExternalEntryActivity.this.transaction.setAccountId(realId);
					String currencySymbol = CurrencyManager.getSymbol(db, theAccount.currency);
			    	((TextView) findViewById(R.id.currencySymbol)).setText(currencySymbol);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

    	});
    	accountsSpinner.setAdapter(adapter);
    }

    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }

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
     * Update the date button with the latest value.
     */

    private void updateDate() {
    	Button button = (Button) findViewById(R.id.newEntryDateButton);
    	SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM, yyyy");
    	String date = sdf.format(new Date(transaction.getTimestamp()));
		button.setText(date);
    }

    /**
     * Listener for when the user finishes setting the date.
     */
    private class DateListener implements OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int month, int day) {
        	Calendar cal = Calendar.getInstance();
        	cal.set(year, month, day);
        	ExternalEntryActivity.this.transaction.setTimestamp(cal.getTime().getTime());
        	ExternalEntryActivity.this.updateDate();
        }
    }

    /**
     * Store the account details into the database.
     */

    public void storeEntryDetails() {
    	EditText editText = (EditText) findViewById(R.id.payee);
    	transaction.setPayee(editText.getText().toString());

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

    	long amount = 0;
    	editText = (EditText) findViewById(R.id.amountMajor);
    	String amountString = editText.getText().toString();
    	if(amountString != null && amountString.length() > 0) {
    		amount += Long.parseLong(amountString) * 100;
    	}

    	editText = (EditText) findViewById(R.id.amountMinor);
    	if(amountString != null && amountString.length() > 0) {
    		amountString = editText.getText().toString();
        	amount += Long.parseLong(amountString);
    	}
    	if( type == Transaction.TYPE_DEBIT ) {
    		amount = 0 - amount;
    	}
    	transaction.setAmount(amount);

    	AutoCompleteTextView categoryEntry =
    		(AutoCompleteTextView) findViewById(R.id.category);
    	String category = categoryEntry.getText().toString();

    	if(StringUtils.isEmpty(category)) {
    		category = CategoryManager.UNCAT_CAT;
    	}
		int categoryId = CategoryManager.getByName(db, category);
		transaction.setCategoryId(categoryId);

   		TransactionManager.create(db, transaction);
    }


    /**
     * Suggestions adapter for payees.
     */

    public class AccountsAdapter extends SimpleCursorAdapter {

    	public AccountsAdapter(final Context context, final int layout, final Cursor c,
    			final String[] from, final int[] to) {
    		super(context, layout, c, from, to);
    	}

    	@Override
    	public String convertToString(final Cursor cursor)
    	{
    		return cursor.getString(1);
    	}
    }
}