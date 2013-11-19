package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.CategoryManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.PayeeManager;
import com.funkyandroid.banking.android.data.Transaction;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.ui.AmountEventListener;
import com.funkyandroid.banking.android.utils.CurrencyTextKeyListener;
import com.funkyandroid.banking.android.utils.MenuUtil;
import com.funkyandroid.banking.android.utils.StringUtils;
import com.funkyandroid.banking.android.utils.ValueUtils;

public class EditEntryActivity extends ActionBarActivity {

	/**
	 * The transaction being edited.
	 */

	private Transaction transaction;

	/**
	 * Whether or not the transaction was fetched from the database.
	 */

	private boolean fetched = false;

	/**
	 * The Database connection
	 */

	private SQLiteDatabase db;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle(R.string.entryTitleText);
        setContentView(R.layout.edit_entry);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Button button = (Button) findViewById(R.id.newEntryDateButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				@Override
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
        				@Override
						public void onClick(final View view) {
        					storeEntryDetails();
        				}
        		});

		button = (Button) findViewById(R.id.cancelButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				@Override
						public void onClick(final View view) {
        					if( fetched ) {
        				    	SQLiteDatabase db = (new DBHelper(EditEntryActivity.this)).getWritableDatabase();
        						try {
       					    		TransactionManager.delete(db, transaction, false);
        						} finally {
        							db.close();
        						}
        					}
        					EditEntryActivity.this.finish();
        				}
        		});

        EditText editText = (EditText) findViewById(R.id.amount);
        editText.setKeyListener(new CurrencyTextKeyListener());
        AmountEventListener amountEventListener =
        	new AmountEventListener(editText.getOnFocusChangeListener());
        editText.setOnFocusChangeListener(amountEventListener);

    	db = (new DBHelper(this)).getWritableDatabase();

        CategorySuggestionsAdapter categorySuggester =
    		new CategorySuggestionsAdapter(
					this,
					android.R.layout.simple_dropdown_item_1line,
					null,
					CategoryManager.NAME_COL,
					new int[] {android.R.id.text1},
					db);
    	AutoCompleteTextView categoryEntry = (AutoCompleteTextView) findViewById(R.id.category);
    	categoryEntry.setAdapter(categorySuggester);

        PayeeSuggestionsAdapter payeeSuggester =
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

	    	createNew(accountId);
    	} else {
    		transaction = TransactionManager.getById(db, transactionId);
    		if( transaction == null ) {
    			finish();
    			return;
    		}

    		populateWithTransaction();
    	}

    	updateDate();
    }

    /**
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case android.R.id.home:
	    	{
				finish();
				return true;
	    	}
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

    /**
     * Creates a new, empty transaction
     */

    private void createNew(final int accountId) {
		transaction = new Transaction();
		transaction.setAccountId(accountId);
		transaction.setTimestamp(System.currentTimeMillis());

		RadioButton button = (RadioButton) findViewById(R.id.debitButton);
		button.setSelected(true);

		fetched = false;
    }

    /**
     * Fetches a transaction from the database an populates the screen.
     *
     */

    private void populateWithTransaction() {
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

		EditText editText = (EditText) findViewById(R.id.payee);
		editText.setText(transaction.getPayee());

		long amount = transaction.getAmount();
		if( amount < 0 ) {
			amount = 0 - amount;
		}

    	editText = (EditText) findViewById(R.id.amount);
    	editText.setText(ValueUtils.toString(amount, false));

    	Button button = (Button) findViewById(R.id.okButton);
    	button.setText(R.string.updateButtonText);
    	button = (Button) findViewById(R.id.cancelButton);
    	button.setText(R.string.deleteButtonText);

    	String category = CategoryManager.getById(db, transaction.getCategoryId());
    	if(CategoryManager.UNCAT_CAT.equals(category)) {
    		category = "";
    	}
    	TextView categoryEntry = (TextView) findViewById(R.id.category);
    	categoryEntry.setText(category);

    	fetched = true;
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
        @Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
        	Calendar cal = Calendar.getInstance();
        	cal.set(year, month, day);
        	EditEntryActivity.this.transaction.setTimestamp(cal.getTime().getTime());
        	EditEntryActivity.this.updateDate();
        }
    }

    /**
     * Store the account details into the database.
     */

    public void storeEntryDetails() {
    	try {
	    	EditText editText = (EditText) findViewById(R.id.payee);
	    	transaction.setPayee(editText.getText().toString());

	    	RadioGroup transactionType = (RadioGroup) findViewById(R.id.type);

	    	int selected = transactionType.getCheckedRadioButtonId();
	    	int type;
	    	if( selected == R.id.debitButton) {
	    		type = Transaction.TYPE_DEBIT;
	    	} else if ( selected == R.id.creditButton) {
				type = Transaction.TYPE_CREDIT;
	    	} else {
				throw new RuntimeException("Unknown button ID"+transactionType.getCheckedRadioButtonId());
	    	}
	    	transaction.setType(type);

	    	long oldAmount = transaction.getAmount();

	    	editText = (EditText) findViewById(R.id.amount);
	    	final String amountString = editText.getText().toString();
	    	long amount = ValueUtils.toLong(amountString);
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

		   	if( fetched ) {
		   		TransactionManager.update(db, transaction, oldAmount);
	    	} else {
	    		TransactionManager.create(db, transaction);
	    	}
			finish();
    	} catch( NumberFormatException ex ) {
    		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
    									.setTitle("Your entry could not be stored")
    									.setMessage(ex.getMessage())
    									.setPositiveButton("OK", null)
    									.show();
    	}
    }
}