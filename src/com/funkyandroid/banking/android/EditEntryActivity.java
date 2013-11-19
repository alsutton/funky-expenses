package com.funkyandroid.banking.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.funkyandroid.banking.android.data.CategoryManager;
import com.funkyandroid.banking.android.data.DBHelper;
import com.funkyandroid.banking.android.data.PayeeManager;
import com.funkyandroid.banking.android.data.Transaction;
import com.funkyandroid.banking.android.data.TransactionManager;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.ui.AmountEventListener;
import com.funkyandroid.banking.android.utils.CurrencyTextKeyListener;
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
     * The default drawable for an unselected button.
     */

    private Drawable mUnselectedBackground;

    /**
     * Which option is currently selected
     */

    private int mCurrentTransactionTypeSelection;

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

        mUnselectedBackground = findViewById(R.id.creditButton).getBackground();

        findViewById(R.id.creditButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCreditButton();
            }
        });
        findViewById(R.id.debitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDebitButton();
            }
        });

        findViewById(R.id.newEntryDateButton).setOnClickListener(
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
     * Set up the save/cancel/delete menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_entry_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Set up the save/cancel/delete menu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int okTextResource, cancelTextResource, cancelIconResource;
        if(fetched) {
            okTextResource = R.string.updateButtonText;
            cancelTextResource = R.string.deleteButtonText;
            cancelIconResource = R.drawable.ic_1_navigation_cancel;
        } else {
            okTextResource = R.string.okButtonText;
            cancelTextResource = R.string.cancelButtonText;
            cancelIconResource = R.drawable.ic_5_content_discard;
        }

        menu.findItem(R.id.menu_done).setTitle(okTextResource);

        MenuItem cancelItem = menu.findItem(R.id.menu_cancel);
        cancelItem.setTitle(cancelTextResource);
        cancelItem.setIcon(cancelIconResource);

        return true;
    }

    /**
     * Handle the selection of an option.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_done:
                storeEntryDetails();
                finish();
                return true;

            case R.id.menu_cancel:
                if (fetched) {
                    SQLiteDatabase db = (new DBHelper(EditEntryActivity.this)).getWritableDatabase();
                    try {
                        TransactionManager.delete(db, transaction, false);
                    } finally {
                        db.close();
                    }
                }
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
     * Creates a new, empty transaction
     */

    private void createNew(final int accountId) {
		transaction = new Transaction();
		transaction.setAccountId(accountId);
		transaction.setTimestamp(System.currentTimeMillis());

        selectDebitButton();

		fetched = false;
        invalidateOptionsMenu();
    }

    /**
     * Mark the credit button as selected
     */

    private void selectCreditButton() {
        findViewById(R.id.creditButton).setBackgroundResource(R.color.button_option_selected);
        findViewById(R.id.debitButton).setBackground(mUnselectedBackground);
        mCurrentTransactionTypeSelection = R.id.creditButton;
    }

    /**
     * Mark the debit button as selected
     */

    private void selectDebitButton() {
        findViewById(R.id.debitButton).setBackgroundResource(R.color.button_option_selected);
        findViewById(R.id.creditButton).setBackground(mUnselectedBackground);
        mCurrentTransactionTypeSelection = R.id.debitButton;
    }

    /**
     * Fetches a transaction from the database an populates the screen.
     *
     */

    private void populateWithTransaction() {
		switch(transaction.getType()) {
			case	Transaction.TYPE_CREDIT:
			{
				selectCreditButton();
				break;
			}
			case	Transaction.TYPE_DEBIT:
			{
				selectDebitButton();
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

    	String category = CategoryManager.getById(db, transaction.getCategoryId());
    	if(CategoryManager.UNCAT_CAT.equals(category)) {
    		category = "";
    	}
    	TextView categoryEntry = (TextView) findViewById(R.id.category);
    	categoryEntry.setText(category);

    	fetched = true;
        invalidateOptionsMenu();
    }

    /**
     * Update the date button with the latest value.
     */

    private void updateDate() {
    	SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM, yyyy");
        ((EditText) findViewById(R.id.newEntryDateButton)).setText(sdf.format(new Date(transaction.getTimestamp())));
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

	    	int type;
	    	if( mCurrentTransactionTypeSelection == R.id.creditButton) {
	    		type = Transaction.TYPE_CREDIT;
	    	} else {
				type = Transaction.TYPE_DEBIT;
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
    	} catch( NumberFormatException ex ) {
    		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
    									.setTitle("Your entry could not be stored")
    									.setMessage(ex.getMessage())
    									.setPositiveButton("OK", null)
    									.show();
    	}
    }
}