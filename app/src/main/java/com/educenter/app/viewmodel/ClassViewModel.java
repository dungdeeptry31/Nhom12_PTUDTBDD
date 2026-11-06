package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.model.ClassRoom;
import com.educenter.app.model.Student;
import com.educenter.app.repository.ClassRepository;
import com.educenter.app.repository.DataCallback;

import java.util.List;

/**
 * ViewModel for Class management, enrollment actions, and roster inspection.
 */
public class ClassViewModel extends AndroidViewModel {

    private final ClassRepository classRepository;

    private final MutableLiveData<List<ClassRoom>> classesLiveData = new MutableLiveData<>();
    private final MutableLiveData<ClassRoom> selectedClassLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Student>> classStudentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionSuccessLiveData = new MutableLiveData<>();

    public ClassViewModel(@NonNull Application application) {
        super(application);
        this.classRepository = new ClassRepository(application);
    }

    public LiveData<List<ClassRoom>> getClassesLiveData() {
        return classesLiveData;
    }

    public LiveData<ClassRoom> getSelectedClassLiveData() {
        return selectedClassLiveData;
    }

    public LiveData<List<Student>> getClassStudentsLiveData() {
        return classStudentsLiveData;
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

    public void loadClasses(String keyword, Long subjectId, Long teacherId, String status) {
        isLoadingLiveData.setValue(true);
        classRepository.searchClasses(keyword, subjectId, teacherId, status, new DataCallback<List<ClassRoom>>() {
            @Override
            public void onSuccess(List<ClassRoom> result) {
                isLoadingLiveData.setValue(false);
                classesLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void loadClassDetail(long classId) {
        isLoadingLiveData.setValue(true);
        classRepository.getClassById(classId, new DataCallback<ClassRoom>() {
            @Override
            public void onSuccess(ClassRoom classRoom) {
                isLoadingLiveData.setValue(false);
                selectedClassLiveData.setValue(classRoom);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });

        classRepository.getStudentsInClass(classId, new DataCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> students) {
                classStudentsLiveData.setValue(students);
            }

            @Override
            public void onError(String message) {
                // Ignore
            }
        });
    }

    public void insertClass(ClassRoom classRoom) {
        isLoadingLiveData.setValue(true);
        classRepository.insertClass(classRoom, new DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Tạo lớp học mới thành công!");
                loadClasses("", null, null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void updateClass(ClassRoom classRoom) {
        isLoadingLiveData.setValue(true);
        classRepository.updateClass(classRoom, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Cập nhật thông tin lớp học thành công!");
                loadClasses("", null, null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void deleteClass(long id) {
        isLoadingLiveData.setValue(true);
        classRepository.deleteClass(id, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Xóa lớp học thành công!");
                loadClasses("", null, null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void enrollStudent(long classId, long studentId) {
        isLoadingLiveData.setValue(true);
        classRepository.enrollStudent(classId, studentId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Xếp học sinh vào lớp thành công!");
                loadClassDetail(classId);
                loadClasses("", null, null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void unenrollStudent(long classId, long studentId) {
        isLoadingLiveData.setValue(true);
        classRepository.unenrollStudent(classId, studentId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Đã hủy xếp lớp học sinh!");
                loadClassDetail(classId);
                loadClasses("", null, null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }
}
