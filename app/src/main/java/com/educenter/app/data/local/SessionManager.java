package com.educenter.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.educenter.app.utils.Constants;

/**
 * Manages User Session and Login state via SharedPreferences.
 */
public class SessionManager {

    private static SessionManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void createLoginSession(long userId, String username, String fullName, String role, boolean remember) {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putLong(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_FULL_NAME, fullName);
        editor.putString(Constants.KEY_ROLE, role);
        editor.putBoolean(Constants.KEY_REMEMBER_LOGIN, remember);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public boolean isRememberLogin() {
        return pref.getBoolean(Constants.KEY_REMEMBER_LOGIN, false);
    }

    public long getUserId() {
        return pref.getLong(Constants.KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(Constants.KEY_USERNAME, "");
    }

    public String getFullName() {
        return pref.getString(Constants.KEY_FULL_NAME, "");
    }

    public String getUserRole() {
        return pref.getString(Constants.KEY_ROLE, "");
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equalsIgnoreCase(getUserRole());
    }

    public boolean isTeacher() {
        return Constants.ROLE_TEACHER.equalsIgnoreCase(getUserRole());
    }

    public boolean isStaff() {
        return Constants.ROLE_STAFF.equalsIgnoreCase(getUserRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
