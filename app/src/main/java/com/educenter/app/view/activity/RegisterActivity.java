package com.educenter.app.view.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.educenter.app.R;
import com.educenter.app.databinding.ActivityRegisterBinding;
import com.educenter.app.utils.Validator;
import com.educenter.app.viewmodel.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Register Activity handling new staff/user account registration.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.tvLoginPrompt.setOnClickListener(v -> finish());

        // Clear errors on typing
        binding.etFullName.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilFullName.setError(null)));
        binding.etUsername.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilUsername.setError(null)));
        binding.etEmail.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilEmail.setError(null)));
        binding.etPhone.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilPhone.setError(null)));
        binding.etPassword.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilPassword.setError(null)));
        binding.etConfirmPassword.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilConfirmPassword.setError(null)));

        binding.btnRegister.setOnClickListener(v -> performRegister());
    }

    private void performRegister() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String username = binding.etUsername.getText() != null ? binding.etUsername.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";

        boolean hasError = false;

        if (!Validator.isValidFullName(fullName)) {
            binding.tilFullName.setError("Họ và tên phải có ít nhất 2 ký tự");
            hasError = true;
        }

        if (!Validator.isValidUsername(username)) {
            binding.tilUsername.setError("Tên đăng nhập từ 3-30 ký tự, không chứa ký tự đặc biệt");
            hasError = true;
        }

        if (Validator.isNotEmpty(email) && !Validator.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            hasError = true;
        }

        if (Validator.isNotEmpty(phone) && !Validator.isValidPhone(phone)) {
            binding.tilPhone.setError(getString(R.string.error_invalid_phone));
            hasError = true;
        }

        if (!Validator.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.error_password_min));
            hasError = true;
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        authViewModel.register(fullName, username, email, phone, password);
    }

    private void observeViewModel() {
        authViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnRegister.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
            }
        });

        authViewModel.getSuccessMessageLiveData().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Thành công")
                        .setMessage(msg)
                        .setPositiveButton("Đăng nhập ngay", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        });

        // TC05: Duplicate username error
        authViewModel.getErrorMessageLiveData().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Lỗi đăng ký")
                        .setMessage(errorMsg)
                        .setPositiveButton("Đồng ý", null)
                        .show();
            }
        });
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onTextChange;
        SimpleTextWatcher(Runnable onTextChange) { this.onTextChange = onTextChange; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onTextChange.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
