package com.funkyandroid.banking.android;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.funkyandroid.banking.android.expenses.demo.R;

public class Preferences extends SherlockPreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addPreferencesFromResource(R.xml.prefs);
    }
}
