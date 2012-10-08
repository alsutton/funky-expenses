package com.funkyandroid.banking.android;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import com.funkyandroid.banking.android.data.*;
import com.funkyandroid.banking.android.expenses.demo.R;
import com.funkyandroid.banking.android.ui.AmountEventListener;
import com.funkyandroid.banking.android.utils.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExternalEntryFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    /**
     * The query to get information about all of the accounts
     */

    private static final String GET_ALL_ACCOUNTS_QUERY =
            "SELECT a._id, a.name, a.opening_balance, a.balance, c.symbol "+
                    "  FROM "+DBHelper.ACCOUNTS_TABLE_NAME+" a, "+DBHelper.CURRENCIES_TABLE_NAME+" c "+
                    " WHERE a.currency = c.short_code ORDER BY name ASC";


    /**
	 * Shared empty String.
	 */

	private static final String EMPTY_STRING = "";

	/**
	 * The transaction being edited.
	 */

	private Transaction transaction;

	/**
	 * The account the transaction is related to
	 */

	private Account account;

	/**
	 * The accounts spinner.
	 */

	private Spinner accountsSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_external_entry, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

		Button button = (Button) getView().findViewById(R.id.newEntryDateButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				@Override
						public void onClick(final View view) {
        		        	Calendar cal = Calendar.getInstance();
        		        	cal.setTime(new Date(ExternalEntryFragment.this.transaction.getTimestamp()));

        		            new DatePickerDialog(
        		            		getActivity(),
        		            		new DateListener(),
        		            		cal.get(Calendar.YEAR),
        		                    cal.get(Calendar.MONTH),
        		                    cal.get(Calendar.DAY_OF_MONTH)).show();
        				}
        		});

		button = (Button) getView().findViewById(R.id.okButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				@Override
						public void onClick(final View view) {
        					storeEntryDetails();
        					getActivity().finish();
        				}
        		});

		button = (Button) getView().findViewById(R.id.cancelButton);
        button.setOnClickListener(
        		new View.OnClickListener() {
        				@Override
						public void onClick(final View view) {
        					getActivity().finish();
        				}
        		});

        EditText editText = (EditText) getView().findViewById(R.id.amount);
        editText.setKeyListener(new CurrencyTextKeyListener());
        AmountEventListener amountEventListener =
        	new AmountEventListener(editText.getOnFocusChangeListener());
        editText.setOnFocusChangeListener(amountEventListener);

        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Get the account details when started.
     */

    @Override
    public void onStart() {
    	super.onStart();

        final Bundle arguments = super.getArguments();
        final View rootView = getView();

    	((TextView) rootView.findViewById(R.id.currencySymbol)).setText("");

		transaction = new Transaction();
		transaction.setAccountId(-1);

		long date = arguments.getLong("com.funkyandroid.DATE", -1);
		if( date == -1) {
			date = System.currentTimeMillis();
		}
		transaction.setTimestamp(date);
    	updateDate();

		String payee = arguments.getString("com.funkyandroid.PAYEE");
		if( payee == null || payee.length() == 0 ) {
			((TextView)rootView.findViewById(R.id.payee)).setText(EMPTY_STRING);
		} else {
			((TextView)rootView.findViewById(R.id.payee)).setText(payee);
		}

		String category = arguments.getString("com.funkyandroid.CATEGORY");
		if( category == null || category.length() == 0 ) {
			((TextView)rootView.findViewById(R.id.category)).setText(EMPTY_STRING);
		} else {
			((TextView)rootView.findViewById(R.id.category)).setText(category);

		}

		String amount = arguments.getString("com.funkyandroid.AMOUNT");
		TextView amountField = (TextView)rootView.findViewById(R.id.amount);
		if( amount != null && amount.length() > 0 ) {
			amountField.setText(amount);
		} else {
			amountField.setText(ValueUtils.getZeroValueString());
		}

		RadioButton button = (RadioButton) rootView.findViewById(R.id.debitButton);
		button.setSelected(true);

    	accountsSpinner = (Spinner)getView().findViewById(R.id.accountSpinner);
    	accountsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view,
                                       final int position, final long id) {
                int realId = (int)id;
                SQLiteDatabase db = ((DatabaseReadingActivity) getActivity()).getReadableDatabaseConnection();
                account = AccountManager.getById(db, realId);
                if (account != null) {
                    ExternalEntryFragment.this.transaction.setAccountId(realId);
                    String currencySymbol = CurrencyManager.getSymbol(db, account.currency);
                    ((TextView) getView().findViewById(R.id.currencySymbol)).setText(currencySymbol);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });


        SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
        CategorySuggestionsAdapter categorySuggester =
                new CategorySuggestionsAdapter(
                        getActivity(),
                        android.R.layout.simple_dropdown_item_1line,
                        null,
                        CategoryManager.NAME_COL,
                        new int[] {android.R.id.text1},
                        db);
        AutoCompleteTextView categoryEntry = (AutoCompleteTextView) getView().findViewById(R.id.category);
        categoryEntry.setAdapter(categorySuggester);

        PayeeSuggestionsAdapter payeeSuggester =
                new PayeeSuggestionsAdapter(
                        getActivity(),
                        android.R.layout.simple_dropdown_item_1line,
                        null,
                        PayeeManager.NAME_COL,
                        new int[] {android.R.id.text1},
                        db );
        AutoCompleteTextView payeeEntry = (AutoCompleteTextView) getView().findViewById(R.id.payee);
        payeeEntry.setAdapter(payeeSuggester);

    }

    /**
     * Create the loader for the cursor.
     *
     * @param id The ID of the loader.
     * @param args The arguments for the query.
     *
     * @return The loader.
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final SQLiteDatabase db = ((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection();
        return new DatabaseRawQueryCursorLoader(getActivity(), db, GET_ALL_ACCOUNTS_QUERY, new String[] { Integer.toString(account.id) } );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        AccountsAdapter adapter = new AccountsAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                data,
                AccountManager.NAME_COL,
                new int[] {android.R.id.text1} );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountsSpinner.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        accountsSpinner.setAdapter(null);
    }



    /**
     * Update the date button with the latest value.
     */

    private void updateDate() {
    	Button button = (Button) getView().findViewById(R.id.newEntryDateButton);
    	SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM, yyyy");
    	String date = sdf.format(new Date(transaction.getTimestamp()));
		button.setText(date);
    }

    /**
     * Listener for when the user finishes setting the date.
     */
    private class DateListener implements OnDateSetListener {
        @Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
        	Calendar cal = Calendar.getInstance();
        	cal.set(year, month, day);
        	ExternalEntryFragment.this.transaction.setTimestamp(cal.getTime().getTime());
        	ExternalEntryFragment.this.updateDate();
        }
    }

    /**
     * Store the account details into the database.
     */

    public void storeEntryDetails() {
        View rootView = getView();

    	EditText editText = (EditText) rootView.findViewById(R.id.payee);
    	transaction.setPayee(editText.getText().toString());

    	RadioGroup transactionType = (RadioGroup) rootView.findViewById(R.id.type);

    	int type;
    	int selected = transactionType.getCheckedRadioButtonId();
    	if			( selected == R.id.debitButton ) {
    		type = Transaction.TYPE_DEBIT;
    	} else if	( selected ==  R.id.creditButton ) {
			type = Transaction.TYPE_CREDIT;
    	} else {
			throw new RuntimeException("Unknown button ID"+transactionType.getCheckedRadioButtonId());
    	}
    	transaction.setType(type);

    	editText = (EditText) rootView.findViewById(R.id.amount);
    	final String amountString = editText.getText().toString();
    	long amount = ValueUtils.toLong(amountString);
    	if( type == Transaction.TYPE_DEBIT ) {
    		amount = 0 - amount;
    	}
    	transaction.setAmount(amount);

    	AutoCompleteTextView categoryEntry =
    		(AutoCompleteTextView) rootView.findViewById(R.id.category);
    	String category = categoryEntry.getText().toString();

    	if(StringUtils.isEmpty(category)) {
    		category = CategoryManager.UNCAT_CAT;
    	}
		int categoryId = CategoryManager.getByName(((DatabaseReadingActivity)getActivity()).getReadableDatabaseConnection(), category);
		transaction.setCategoryId(categoryId);

        SQLiteDatabase writeableDb = (new DBHelper(getActivity())).getReadableDatabase();
        try {
   		    TransactionManager.create(writeableDb, transaction);
        } finally {
            writeableDb.close();
        }
    }


    /**
     * Suggestions adapter for payees.
     */

    public class AccountsAdapter extends SimpleCursorAdapter {

    	public AccountsAdapter(final Context context, final int layout, final Cursor c,
    			final String[] from, final int[] to) {
    		super(context, layout, c, from, to, 0);
    	}

    	@Override
    	public String convertToString(final Cursor cursor)
    	{
    		return cursor.getString(1);
    	}
    }
}