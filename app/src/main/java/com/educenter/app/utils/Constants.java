package com.educenter.app.utils;

/**
 * Global Constants for EduCenter application.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // Database
    public static final String DATABASE_NAME = "educenter.db";
    public static final int DATABASE_VERSION = 3; // Bumped to 3 for clean auto-upgrade and fresh seeding

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STAFF = "STAFF";

    // Class Statuses
    public static final String STATUS_CLASS_OPEN = "OPEN";
    public static final String STATUS_CLASS_UPCOMING = "UPCOMING";
    public static final String STATUS_CLASS_FINISHED = "FINISHED";

    // Attendance Statuses
    public static final String ATTENDANCE_PRESENT = "PRESENT";
    public static final String ATTENDANCE_ABSENT = "ABSENT";
    public static final String ATTENDANCE_LATE = "LATE";

    // Tuition Statuses
    public static final String TUITION_PAID = "PAID";
    public static final String TUITION_PARTIAL = "PARTIALLY_PAID";
    public static final String TUITION_UNPAID = "UNPAID";

    // Payment Methods
    public static final String PAYMENT_METHOD_CASH = "CASH";
    public static final String PAYMENT_METHOD_TRANSFER = "TRANSFER";

    // Student / User Statuses
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    // SharedPreferences
    public static final String PREF_NAME = "educenter_session_pref";
    public static final String KEY_IS_LOGGED_IN = "key_is_logged_in";
    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_USERNAME = "key_username";
    public static final String KEY_FULL_NAME = "key_full_name";
    public static final String KEY_ROLE = "key_role";
    public static final String KEY_REMEMBER_LOGIN = "key_remember_login";
}
