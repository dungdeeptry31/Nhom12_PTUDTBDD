package com.educenter.app.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility for formatting Vietnamese currency (VND) according to standard Vietnamese locale.
 */
public final class CurrencyUtils {

    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");

    private CurrencyUtils() {
        // Utility class
    }

    /**
     * Formats a double amount to Vietnamese Currency format (e.g. 1.500.000 ₫).
     *
     * @param amount Amount to format
     * @return Formatted string with currency symbol
     */
    public static String formatVnd(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);
        return currencyFormat.format(amount);
    }

    /**
     * Formats a double amount to plain number with dots (e.g. 1.500.000).
     *
     * @param amount Amount to format
     * @return Formatted number string
     */
    public static String formatNumber(double amount) {
        NumberFormat numberFormat = NumberFormat.getInstance(VIETNAM_LOCALE);
        return numberFormat.format(amount);
    }

    /**
     * Parses a string representation of amount into a double.
     *
     * @param amountStr Input string (can contain dots, commas, currency symbol)
     * @return Parsed double value, or 0.0 on error
     */
    public static double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }
        try {
            // Remove non-digit characters except period/comma
            String clean = amountStr.replaceAll("[^0-9]", "");
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
