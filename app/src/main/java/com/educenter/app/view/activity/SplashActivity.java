package com.educenter.app.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.educenter.app.data.local.SessionManager;
import com.educenter.app.databinding.ActivitySplashBinding;

/**
 * Splash Screen Activity.
 * Displays application logo, brand and slogan, checks active user session,
 * and navigates accordingly to LoginActivity or MainActivity.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1500;
    private ActivitySplashBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkNavigation, SPLASH_DELAY_MS);
    }

    private void checkNavigation() {
        Intent intent;
        if (sessionManager.isLoggedIn() && sessionManager.isRememberLogin()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
