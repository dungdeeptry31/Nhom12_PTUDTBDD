package com.educenter.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.educenter.app.R;
import com.educenter.app.databinding.ActivityLoginBinding;
import com.educenter.app.utils.Validator;
import com.educenter.app.viewmodel.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Login Activity providing credentials entry, input validation,
 * quick demo accounts auto-fill, and role-based entry point.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Clear errors on typing
        binding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilUsername.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Submit login
        binding.btnLogin.setOnClickListener(v -> performLogin());

        // Quick Demo: Admin (TC01)
        binding.btnDemoAdmin.setOnClickListener(v -> {
            binding.etUsername.setText("admin");
            binding.etPassword.setText("123456");
            binding.tilUsername.setError(null);
            binding.tilPassword.setError(null);
        });

        // Quick Demo: Teacher (TC22)
        binding.btnDemoTeacher.setOnClickListener(v -> {
            binding.etUsername.setText("giaovien");
            binding.etPassword.setText("123456");
            binding.tilUsername.setError(null);
            binding.tilPassword.setError(null);
        });

        // Quick Demo: Locked Account (TC03)
        binding.btnDemoLocked.setOnClickListener(v -> {
            binding.etUsername.setText("bikhoa");
            binding.etPassword.setText("123456");
            binding.tilUsername.setError(null);
            binding.tilPassword.setError(null);
        });

        // Go to Register
        binding.tvRegisterPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String username = binding.etUsername.getText() != null ? binding.etUsername.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        boolean remember = binding.cbRemember.isChecked();

        // TC04: Validate empty inputs
        boolean hasError = false;
        if (!Validator.isNotEmpty(username)) {
            binding.tilUsername.setError(getString(R.string.error_empty_field));
            hasError = true;
        }

        if (!Validator.isNotEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_empty_field));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        authViewModel.login(username, password, remember);
    }

    private void observeViewModel() {
        authViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnLogin.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
            }
        });

        // TC01: Login Success -> Navigate to MainActivity
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // TC02 / TC03: Login Error -> Show Vietnamese alert dialog
        authViewModel.getErrorMessageLiveData().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Thông báo")
                        .setMessage(errorMsg)
                        .setPositiveButton("Đồng ý", null)
                        .show();
            }
        });
    }
}
