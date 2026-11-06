package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.data.local.SessionManager;
import com.educenter.app.model.User;
import com.educenter.app.repository.AuthRepository;
import com.educenter.app.repository.DataCallback;

/**
 * ViewModel for Authentication, Registration, and Profile settings.
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successMessageLiveData = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
        this.sessionManager = SessionManager.getInstance(application);
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<String> getSuccessMessageLiveData() {
        return successMessageLiveData;
    }

    public void login(String username, String password, boolean remember) {
        isLoadingLiveData.setValue(true);
        authRepository.login(username, password, remember, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                isLoadingLiveData.setValue(false);
                userLiveData.setValue(user);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void register(String fullName, String username, String email, String phone, String password) {
        isLoadingLiveData.setValue(true);
        authRepository.register(fullName, username, email, phone, password, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                successMessageLiveData.setValue("Đăng ký tài khoản thành công! Bạn có thể đăng nhập ngay.");
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void updateProfile(long userId, String fullName, String email, String phone) {
        isLoadingLiveData.setValue(true);
        authRepository.updateProfile(userId, fullName, email, phone, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                successMessageLiveData.setValue("Cập nhật thông tin cá nhân thành công!");
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void changePassword(long userId, String oldPassword, String newPassword) {
        isLoadingLiveData.setValue(true);
        authRepository.changePassword(userId, oldPassword, newPassword, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                successMessageLiveData.setValue("Đổi mật khẩu thành công!");
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void logout() {
        sessionManager.logout();
    }
}
