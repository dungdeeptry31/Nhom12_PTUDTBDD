package com.educenter.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.educenter.app.model.ClassRoom;
import com.educenter.app.model.Student;
import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.StudentRepository;

import java.util.List;

/**
 * ViewModel for Student list, search, filter, and CRUD operations.
 */
public class StudentViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;

    private final MutableLiveData<List<Student>> studentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Student> selectedStudentLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ClassRoom>> enrolledClassesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionSuccessLiveData = new MutableLiveData<>();

    public StudentViewModel(@NonNull Application application) {
        super(application);
        this.studentRepository = new StudentRepository(application);
    }

    public LiveData<List<Student>> getStudentsLiveData() {
        return studentsLiveData;
    }

    public LiveData<Student> getSelectedStudentLiveData() {
        return selectedStudentLiveData;
    }

    public LiveData<List<ClassRoom>> getEnrolledClassesLiveData() {
        return enrolledClassesLiveData;
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

    public void loadStudents(String keyword, String gradeFilter, Integer activeFilter) {
        isLoadingLiveData.setValue(true);
        studentRepository.searchStudents(keyword, gradeFilter, activeFilter, new DataCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> result) {
                isLoadingLiveData.setValue(false);
                studentsLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void loadStudentDetail(long id) {
        isLoadingLiveData.setValue(true);
        studentRepository.getStudentById(id, new DataCallback<Student>() {
            @Override
            public void onSuccess(Student student) {
                isLoadingLiveData.setValue(false);
                selectedStudentLiveData.setValue(student);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });

        studentRepository.getEnrolledClassesForStudent(id, new DataCallback<List<ClassRoom>>() {
            @Override
            public void onSuccess(List<ClassRoom> classes) {
                enrolledClassesLiveData.setValue(classes);
            }

            @Override
            public void onError(String message) {
                // Ignore non-fatal
            }
        });
    }

    public void insertStudent(Student student) {
        isLoadingLiveData.setValue(true);
        studentRepository.insertStudent(student, new DataCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Thêm học sinh thành công!");
                loadStudents("", null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void updateStudent(Student student) {
        isLoadingLiveData.setValue(true);
        studentRepository.updateStudent(student, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Cập nhật thông tin học sinh thành công!");
                loadStudents("", null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
            }
        });
    }

    public void deleteStudent(long id) {
        isLoadingLiveData.setValue(true);
        studentRepository.deleteStudent(id, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoadingLiveData.setValue(false);
                actionSuccessLiveData.setValue("Xóa học sinh thành công!");
                loadStudents("", null, null);
            }

            @Override
            public void onError(String message) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(message);
                loadStudents("", null, null);
            }
        });
    }
}
