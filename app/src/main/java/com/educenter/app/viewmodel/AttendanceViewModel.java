package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.model.Attendance;
import com.educenter.app.repository.AttendanceRepository;
import com.educenter.app.repository.DataCallback;

import java.util.List;

/**
 * ViewModel for Classroom Attendance tracking and batch actions.
 */
public class AttendanceViewModel extends AndroidViewModel {

    private final AttendanceRepository attendanceRepository;

    private final MutableLiveData<List<Attendance>> attendanceListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionSuccessLiveData = new MutableLiveData<>();

    public AttendanceViewModel(@NonNull Application application) {
        super(application);
        this.attendanceRepository = new AttendanceRepository(application);
    }

    public LiveData<List<Attendance>> getAttendanceListLiveData() {
        return attendanceListLiveData;
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

    public void loadAttendance(long classId, String date) {
        isLoadingLiveData.setValue(true);
        attendanceRepository.getAttendanceForSession(classId, date, new DataCallback<List<Attendance>>() {
            @Override
            public void onSuccess(List<Attendance> result) {
                isLoadingLiveData.setValue(false);
                attendanceListLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void saveAttendanceBatch(long classId, String date, List<Attendance> list) {
        isLoadingLiveData.setValue(true);
        attendanceRepository.saveAttendanceBatch(classId, date, list, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Lưu điểm danh thành công!");
                loadAttendance(classId, date);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }
}
