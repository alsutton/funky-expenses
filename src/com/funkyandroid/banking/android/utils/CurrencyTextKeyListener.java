package com.funkyandroid.banking.android.utils;

import java.text.DecimalFormatSymbols;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

/**
 * EditText KeyListener which ensures the data is formatted as expected for a
 * currency.
 *
 * @author alsutton
 */
public class CurrencyTextKeyListener extends DigitsKeyListener {
    public CurrencyTextKeyListener() {
        super(false, true);
    }

    private int digits = 2;

    public void setDigits(int d) {
        digits = d;
    }

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		CharSequence out = super.filter(source, start, end, dest, dstart, dend);
		final DecimalFormatSymbols dfs = new DecimalFormatSymbols();

		// if changed, replace the source
		if (out != null) {
			source = out;
			start = 0;
			end = out.length();
		}

		int len = end - start;

		// if deleting, source is empty
		// and deleting can't break anything
		if (len == 0) {
			return source;
		}

		int dlen = dest.length();

		// Find the position of the decimal .
		for (int i = 0; i < dstart; i++) {
			if (dest.charAt(i) == dfs.getDecimalSeparator()) {
				// being here means, that a number has
				// been inserted after the dot
				// check if the amount of digits is right
				return (dlen - (i + 1) + len > digits) ? ""
						: new SpannableStringBuilder(source, start, end);
			}
		}

		for (int i = start; i < end; ++i) {
			if (source.charAt(i) == dfs.getDecimalSeparator()) {
				// being here means, dot has been inserted
				// check if the amount of digits is right
				if ((dlen - dend) + (end - (i + 1)) > digits)
					return "";
				else
					break; // return new SpannableStringBuilder(source, start,
							// end);
			}
		}

		// if the dot is after the inserted part,
		// nothing can break
		return new SpannableStringBuilder(source, start, end);
	}
}
