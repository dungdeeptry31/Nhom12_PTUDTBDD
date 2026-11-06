package com.educenter.app;

import android.app.Application;
import android.util.Log;

import com.educenter.app.data.local.DatabaseHelper;
import com.educenter.app.data.local.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Application Class for EduCenter.
 * Responsible for initializing global singleton instances such as DatabaseHelper
 * and SessionManager, and pre-warming SQLite on background thread.
 */
public class EduCenterApplication extends Application {

    private static final String TAG = "EduCenterApp";
    private static EduCenterApplication instance;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ExecutorService appExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appExecutor = Executors.newFixedThreadPool(4);
        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        // Pre-warm database & trigger seeding on background thread if first launch
        appExecutor.execute(() -> {
            try {
                Log.d(TAG, "Initializing database and verifying seed data...");
                databaseHelper.getWritableDatabase();
                Log.d(TAG, "Database ready.");
            } catch (Exception e) {
                Log.e(TAG, "Database initialization error", e);
            }
        });
    }

    public static synchronized EduCenterApplication getInstance() {
        return instance;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ExecutorService getAppExecutor() {
        return appExecutor;
    }
}
