package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.model.Subject;
import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.SubjectRepository;

import java.util.List;

/**
 * ViewModel for Subject management.
 */
public class SubjectViewModel extends AndroidViewModel {

    private final SubjectRepository subjectRepository;

    private final MutableLiveData<List<Subject>> subjectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionSuccessLiveData = new MutableLiveData<>();

    public SubjectViewModel(@NonNull Application application) {
        super(application);
        this.subjectRepository = new SubjectRepository(application);
    }

    public LiveData<List<Subject>> getSubjectsLiveData() {
        return subjectsLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<String> getActionSuccessLiveData() {
        return actionSuccessLiveData;
    }

    public void loadSubjects() {
        isLoadingLiveData.setValue(true);
        subjectRepository.getAllSubjects(new DataCallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> result) {
                isLoadingLiveData.setValue(false);
                subjectsLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void insertSubject(Subject subject) {
        isLoadingLiveData.setValue(true);
        subjectRepository.insertSubject(subject, new DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Thêm môn học thành công!");
                loadSubjects();
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void updateSubject(Subject subject) {
        isLoadingLiveData.setValue(true);
        subjectRepository.updateSubject(subject, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Cập nhật môn học thành công!");
                loadSubjects();
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void deleteSubject(long id) {
        isLoadingLiveData.setValue(true);
        subjectRepository.deleteSubject(id, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Xóa môn học thành công!");
                loadSubjects();
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }
}
