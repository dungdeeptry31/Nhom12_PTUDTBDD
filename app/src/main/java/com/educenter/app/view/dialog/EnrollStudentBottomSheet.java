package com.educenter.app.view.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.educenter.app.databinding.DialogEnrollStudentBinding;
import com.educenter.app.model.ClassRoom;
import com.educenter.app.model.Student;
import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.StudentRepository;
import com.educenter.app.viewmodel.ClassViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * BottomSheet Dialog allowing users to enroll a student into a selected class.
 * Enforces capacity checks (TC13) and duplicate prevention (TC14).
 */
public class EnrollStudentBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CLASS = "arg_class";

    private DialogEnrollStudentBinding binding;
    private ClassViewModel classViewModel;
    private ClassRoom targetClass;
    private final List<Student> studentList = new ArrayList<>();
    private long selectedStudentId = -1;

    public static EnrollStudentBottomSheet newInstance(ClassRoom classRoom) {
        EnrollStudentBottomSheet sheet = new EnrollStudentBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLASS, classRoom);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetClass = (ClassRoom) getArguments().getSerializable(ARG_CLASS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogEnrollStudentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        classViewModel = new ViewModelProvider(requireActivity()).get(ClassViewModel.class);

        if (targetClass != null) {
            binding.tvEnrollClassName.setText(targetClass.getName() + " (" + targetClass.getCode() + ")");
            String capText = "Sĩ số: " + targetClass.getCurrentStudents() + " / " + targetClass.getMaxStudents() + " học sinh";
            if (targetClass.isFull()) {
                capText += " (ĐÃ ĐỦ SĨ SỐ)";
            }
            binding.tvEnrollClassCapacity.setText(capText);
        }

        loadStudents();

        binding.btnEnrollClose.setOnClickListener(v -> dismiss());
        binding.btnConfirmEnroll.setOnClickListener(v -> performEnrollment());
    }

    private void loadStudents() {
        StudentRepository studentRepo = new StudentRepository(requireContext());
        studentRepo.getAllStudents(new DataCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> result) {
                studentList.clear();
                studentList.addAll(result);

                List<String> displayList = new ArrayList<>();
                for (Student s : result) {
                    displayList.add(s.getFullName() + " (" + s.getStudentCode() + ") - Khối " + s.getGradeLevel());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, displayList);
                binding.actEnrollStudent.setAdapter(adapter);

                binding.actEnrollStudent.setOnItemClickListener((parent, view, position, id) -> {
                    selectedStudentId = studentList.get(position).getId();
                    binding.tilEnrollStudent.setError(null);
                });

                if (!studentList.isEmpty()) {
                    binding.actEnrollStudent.setText(displayList.get(0), false);
                    selectedStudentId = studentList.get(0).getId();
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void performEnrollment() {
        if (targetClass == null) {
            dismiss();
            return;
        }

        if (selectedStudentId <= 0) {
            binding.tilEnrollStudent.setError("Vui lòng chọn học sinh cần xếp lớp!");
            return;
        }

        classViewModel.enrollStudent(targetClass.getId(), selectedStudentId);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
