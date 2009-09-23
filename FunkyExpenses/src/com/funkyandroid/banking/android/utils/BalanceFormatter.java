package com.funkyandroid.banking.android.utils;

public class BalanceFormatter {
	
	/**
	 * The currency symbol when none is available.
	 */
	
	public static final String UNKNOWN_CURRENCY_SYMBOL = "";

	public static String format(long balance) {
		return BalanceFormatter.format(balance, null);
	}
	
	public static String format(long balance, final String currencySymbol) {
		final StringBuilder builder = new StringBuilder(8);
		format(builder, balance, currencySymbol);
		return builder.toString();
	}

	public static void format(final StringBuilder builder, long balance, final String currencySymbol) {
		if( balance < 0 ) {
			builder.append('-');
			balance = 0-balance;
		}
		
		if(currencySymbol != null) {
			builder.append(currencySymbol);
			builder.append(' ');
		}
		
		builder.append(balance/100);
		builder.append('.');
		long minorCurrency = balance%100;
		if( minorCurrency < 0 ) {
			minorCurrency = 0 - minorCurrency;
		}
		if( minorCurrency < 10 ) {
			builder.append('0');
		}
		builder.append(minorCurrency);
	}
}
