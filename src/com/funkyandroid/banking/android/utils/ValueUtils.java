package com.funkyandroid.banking.android.utils;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ValueUtils {

	/**
	 * Convert a xxx.yyy string into a long containing the value
	 *
	 * @param amountString The String representation of the value
	 * @return The value as a long
	 */

	public static long toLong(final String amountString) {
		if(amountString == null) {
			return 0;
		}

		final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());

		StringBuilder	majorAmountBuilder = new StringBuilder(amountString.length()),
						minorAmountBuilder = new StringBuilder(amountString.length());
		boolean fillingMajor = true;
		for(char c : amountString.toCharArray()) {
			if(c == dfs.getDecimalSeparator()) {
				fillingMajor = false;
			} else if (c != dfs.getGroupingSeparator()) {
				if(fillingMajor) {
					majorAmountBuilder.append(c);
				} else {
					minorAmountBuilder.append(c);
				}
			}
		}

    	long amount = 0;
		final String majorAmount = majorAmountBuilder.toString();
    	if(majorAmount.length() > 0) {
    		amount += Long.parseLong(majorAmount) * 100;
    	}

    	final String minorAmount = minorAmountBuilder.toString();
        int minorMultiplier;
        if(minorAmount.length() == 1) {
            minorMultiplier = 10;
        } else {
            minorMultiplier = 1;
        }
    	if(minorAmount.length() > 0) {
        	amount += (Integer.parseInt(minorAmount)*minorMultiplier);
    	}
    	return amount;
	}

	/**
	 * Convert a long into a String.
	 *
	 * @param value The value
	 * @param padValue Whether or not to add .00 if there are no minor currency units.
	 * @return The String representation
	 */

	public static String toString(final long value, final boolean padValue) {
		final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());

		StringBuilder returnValue = new StringBuilder(16);
		returnValue.append(value/100);

		final long minorValue = value%100;
		if(padValue || minorValue != 0) {
			returnValue.append(dfs.getDecimalSeparator());
			if(minorValue < 10) {
				returnValue.append('0');
			}
			returnValue.append(minorValue);
		}

		return returnValue.toString();
	}

	/**
	 * Get the representation of a 0 value
	 *
	 * @return a 0 value
	 */

	public static String getZeroValueString() {
		return toString(0L, true);
	}
}
