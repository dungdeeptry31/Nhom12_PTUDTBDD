package com.educenter.app.utils;

import java.util.regex.Pattern;

/**
 * Common data validator for inputs across EduCenter.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
    );

    // Vietnamese phone numbers: 10 digits starting with 0
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^0[3|5|7|8|9][0-9]{8}$"
    );

    // Code pattern: Alphanumeric and hyphens, uppercase
    private static final Pattern CODE_PATTERN = Pattern.compile(
            "^[A-Z0-9._-]{2,20}$",
            Pattern.CASE_INSENSITIVE
    );

    private Validator() {
        // Utility class
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidFullName(String name) {
        return isNotEmpty(name) && name.trim().length() >= 2;
    }

    public static boolean isValidUsername(String username) {
        return isNotEmpty(username) && username.trim().length() >= 3 && username.matches("^[a-zA-Z0-9_]{3,30}$");
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && password.length() >= 6;
    }

    public static boolean isValidCode(String code) {
        if (!isNotEmpty(code)) {
            return false;
        }
        return CODE_PATTERN.matcher(code.trim()).matches();
    }

    public static boolean isValidAmount(double amount) {
        return amount > 0;
    }

    public static boolean isValidScore(double score, double maxScore) {
        return score >= 0 && score <= maxScore && maxScore > 0;
    }

    public static boolean isValidCapacity(int capacity) {
        return capacity > 0;
    }
}
