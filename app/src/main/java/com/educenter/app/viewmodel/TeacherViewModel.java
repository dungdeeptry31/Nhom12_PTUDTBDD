package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.model.ClassRoom;
import com.educenter.app.model.Teacher;
import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.TeacherRepository;

import java.util.List;

/**
 * ViewModel for Teacher management and assigned classes.
 */
public class TeacherViewModel extends AndroidViewModel {

    private final TeacherRepository teacherRepository;

    private final MutableLiveData<List<Teacher>> teachersLiveData = new MutableLiveData<>();
    private final MutableLiveData<Teacher> selectedTeacherLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ClassRoom>> assignedClassesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionSuccessLiveData = new MutableLiveData<>();

    public TeacherViewModel(@NonNull Application application) {
        super(application);
        this.teacherRepository = new TeacherRepository(application);
    }

    public LiveData<List<Teacher>> getTeachersLiveData() {
        return teachersLiveData;
    }

    public LiveData<Teacher> getSelectedTeacherLiveData() {
        return selectedTeacherLiveData;
    }

    public LiveData<List<ClassRoom>> getAssignedClassesLiveData() {
        return assignedClassesLiveData;
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

    public void loadTeachers(String keyword, Integer activeFilter) {
        isLoadingLiveData.setValue(true);
        teacherRepository.searchTeachers(keyword, activeFilter, new DataCallback<List<Teacher>>() {
            @Override
            public void onSuccess(List<Teacher> result) {
                isLoadingLiveData.setValue(false);
                teachersLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void loadTeacherDetail(long teacherId) {
        isLoadingLiveData.setValue(true);
        teacherRepository.getTeacherById(teacherId, new DataCallback<Teacher>() {
            @Override
            public void onSuccess(Teacher teacher) {
                isLoadingLiveData.setValue(false);
                selectedTeacherLiveData.setValue(teacher);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });

        teacherRepository.getAssignedClasses(teacherId, new DataCallback<List<ClassRoom>>() {
            @Override
            public void onSuccess(List<ClassRoom> classes) {
                assignedClassesLiveData.setValue(classes);
            }

            @Override
            public void onError(String message) {
                // Ignore
            }
        });
    }

    public void insertTeacher(Teacher teacher) {
        isLoadingLiveData.setValue(true);
        teacherRepository.insertTeacher(teacher, new DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Thêm giáo viên thành công!");
                loadTeachers("", null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void updateTeacher(Teacher teacher) {
        isLoadingLiveData.setValue(true);
        teacherRepository.updateTeacher(teacher, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Cập nhật thông tin giáo viên thành công!");
                loadTeachers("", null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void deleteTeacher(long id) {
        isLoadingLiveData.setValue(true);
        teacherRepository.deleteTeacher(id, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Xóa giáo viên thành công!");
                loadTeachers("", null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }
}
