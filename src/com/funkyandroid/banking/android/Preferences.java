package com.funkyandroid.banking.android;

import android.os.Bundle;

import android.preference.PreferenceActivity;
import com.funkyandroid.banking.android.expenses.demo.R;

public class Preferences extends PreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addPreferencesFromResource(R.xml.prefs);
    }
}
