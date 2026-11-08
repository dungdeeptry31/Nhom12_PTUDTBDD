package com.educenter.app.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.educenter.app.R;
import com.educenter.app.adapter.StudentAdapter;
import com.educenter.app.data.local.SessionManager;
import com.educenter.app.databinding.FragmentStudentBinding;
import com.educenter.app.model.Student;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.view.dialog.ExamResultsBottomSheet;
import com.educenter.app.view.dialog.StudentBottomSheet;
import com.educenter.app.viewmodel.StudentViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Fragment displaying Student list with real-time search, grade filtering, and status display.
 */
public class StudentFragment extends Fragment {

    private FragmentStudentBinding binding;
    private StudentViewModel studentViewModel;
    private StudentAdapter adapter;
    private String selectedGrade = "Tất cả";

    public static StudentFragment newInstance() {
        return new StudentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        studentViewModel = new ViewModelProvider(this).get(StudentViewModel.class);

        setupRecyclerView();
        setupSearchAndFilter();
        observeViewModel();

        binding.fabAddStudent.setOnClickListener(v -> {
            StudentBottomSheet.newInstance(null).show(getChildFragmentManager(), "StudentBottomSheet");
        });

        studentViewModel.loadStudents("", null, null);
    }

    private void setupRecyclerView() {
        adapter = new StudentAdapter(this::onStudentSelected);
        binding.rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStudents.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String kw = s.toString();
                binding.btnClearSearch.setVisibility(kw.isEmpty() ? View.GONE : View.VISIBLE);
                studentViewModel.loadStudents(kw, selectedGrade, null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> binding.etSearchStudent.setText(""));

        binding.chipGroupGrade.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipGrade9)) {
                selectedGrade = "9";
            } else if (checkedIds.contains(R.id.chipGrade11)) {
                selectedGrade = "11";
            } else if (checkedIds.contains(R.id.chipGrade12)) {
                selectedGrade = "12";
            } else {
                selectedGrade = "Tất cả";
            }
            String kw = binding.etSearchStudent.getText() != null ? binding.etSearchStudent.getText().toString() : "";
            studentViewModel.loadStudents(kw, selectedGrade, null);
        });
    }

    private void observeViewModel() {
        studentViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading != null && isLoading ? View.VISIBLE : View.GONE);
        });

        studentViewModel.getStudentsLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.rvStudents.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvStudents.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });

        studentViewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Thông báo")
                        .setMessage(error)
                        .setPositiveButton("Đồng ý", null)
                        .show();
            }
        });

        studentViewModel.getActionSuccessLiveData().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onStudentSelected(Student student) {
        SessionManager session = SessionManager.getInstance(requireContext());
        String[] options;
        if (session.isTeacher()) {
            options = new String[]{
                    "Xem thông tin chi tiết",
                    "Bảng điểm / Kết quả học tập",
                    "Chỉnh sửa thông tin"
            };
        } else {
            options = new String[]{
                    "Xem thông tin chi tiết",
                    "Bảng điểm / Kết quả học tập",
                    "Chỉnh sửa thông tin",
                    "Xóa học sinh"
            };
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(student.getFullName() + " (" + student.getStudentCode() + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Details
                        showStudentDetailsDialog(student);
                    } else if (which == 1) {
                        // Exam results / Academic performance
                        ExamResultsBottomSheet.newInstanceForStudent(student).show(getChildFragmentManager(), "ExamResultsBottomSheet");
                    } else if (which == 2) {
                        // Edit
                        StudentBottomSheet.newInstance(student).show(getChildFragmentManager(), "StudentBottomSheet");
                    } else if (which == 3) {
                        // Delete
                        confirmDeleteStudent(student);
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showStudentDetailsDialog(Student student) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(student.getFullName() + " (" + student.getStudentCode() + ")")
                .setMessage("• Khối: " + student.getGradeLevel()
                        + "\n• Trường: " + student.getSchool()
                        + "\n• Ngày sinh: " + DateUtils.toDisplayDate(student.getDateOfBirth())
                        + "\n• Giới tính: " + student.getGender()
                        + "\n• SĐT học sinh: " + (student.getPhone() != null && !student.getPhone().isEmpty() ? student.getPhone() : "Chưa có")
                        + "\n• Phụ huynh: " + student.getParentName() + " (" + student.getParentPhone() + ")"
                        + "\n• Địa chỉ: " + (student.getAddress() != null && !student.getAddress().isEmpty() ? student.getAddress() : "Chưa cập nhật")
                        + "\n• Số lớp đang tham gia: " + student.getEnrolledClassesCount() + " lớp"
                        + "\n• Trạng thái: " + (student.isActive() ? "Đang theo học" : "Đã nghỉ học"))
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void confirmDeleteStudent(Student student) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận xóa học sinh")
                .setMessage("Bạn có chắc chắn muốn xóa học sinh " + student.getFullName() + " (" + student.getStudentCode() + ") không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    studentViewModel.deleteStudent(student.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
