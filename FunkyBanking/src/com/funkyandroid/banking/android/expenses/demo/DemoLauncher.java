package com.funkyandroid.banking.android.expenses.demo;

import com.funkyandroid.banking.android.AccountsActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DemoLauncher extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		Intent intent = new Intent(this, AccountsActivity.class);
		startActivity(intent);
		finish();    	
    }
}
