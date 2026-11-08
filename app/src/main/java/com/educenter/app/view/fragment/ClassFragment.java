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
import com.educenter.app.adapter.ClassAdapter;
import com.educenter.app.databinding.FragmentClassBinding;
import com.educenter.app.model.ClassRoom;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.CurrencyUtils;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.data.local.SessionManager;
import com.educenter.app.view.dialog.AttendanceBottomSheet;
import com.educenter.app.view.dialog.ClassBottomSheet;
import com.educenter.app.view.dialog.ClassScheduleBottomSheet;
import com.educenter.app.view.dialog.EnrollStudentBottomSheet;
import com.educenter.app.view.dialog.ExamResultsBottomSheet;
import com.educenter.app.viewmodel.ClassViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Fragment displaying Class list, capacity progress bar, status filter, and details.
 */
public class ClassFragment extends Fragment {

    private FragmentClassBinding binding;
    private ClassViewModel classViewModel;
    private ClassAdapter adapter;
    private String selectedStatus = "Tất cả";

    public static ClassFragment newInstance() {
        return new ClassFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClassBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        classViewModel = new ViewModelProvider(this).get(ClassViewModel.class);

        setupRecyclerView();
        setupSearchAndFilter();
        observeViewModel();

        binding.fabAddClass.setOnClickListener(v -> {
            ClassBottomSheet.newInstance(null).show(getChildFragmentManager(), "ClassBottomSheet");
        });

        classViewModel.loadClasses("", null, null, null);
    }

    private void setupRecyclerView() {
        adapter = new ClassAdapter(this::onClassSelected);
        binding.rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClasses.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.etSearchClass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String kw = s.toString();
                binding.btnClearSearch.setVisibility(kw.isEmpty() ? View.GONE : View.VISIBLE);
                classViewModel.loadClasses(kw, null, null, selectedStatus);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> binding.etSearchClass.setText(""));

        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipOpen)) {
                selectedStatus = Constants.STATUS_CLASS_OPEN;
            } else if (checkedIds.contains(R.id.chipUpcoming)) {
                selectedStatus = Constants.STATUS_CLASS_UPCOMING;
            } else if (checkedIds.contains(R.id.chipFinished)) {
                selectedStatus = Constants.STATUS_CLASS_FINISHED;
            } else {
                selectedStatus = "Tất cả";
            }
            String kw = binding.etSearchClass.getText() != null ? binding.etSearchClass.getText().toString() : "";
            classViewModel.loadClasses(kw, null, null, selectedStatus);
        });
    }

    private void observeViewModel() {
        classViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading != null && isLoading ? View.VISIBLE : View.GONE);
        });

        classViewModel.getClassesLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.rvClasses.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvClasses.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });

        classViewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Thông báo")
                        .setMessage(error)
                        .setPositiveButton("Đồng ý", null)
                        .show();
            }
        });

        classViewModel.getActionSuccessLiveData().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onClassSelected(ClassRoom c) {
        SessionManager session = SessionManager.getInstance(requireContext());
        String[] options;
        if (session.isTeacher()) {
            options = new String[]{
                    "Xem chi tiết lớp học",
                    "Thời khóa biểu (Lịch học)",
                    "Bảng điểm / Kết quả học tập",
                    "Điểm danh buổi học"
            };
        } else {
            options = new String[]{
                    "Xem chi tiết lớp học",
                    "Thời khóa biểu (Lịch học)",
                    "Bảng điểm / Kết quả học tập",
                    "Điểm danh buổi học",
                    "Xếp học sinh vào lớp",
                    "Chỉnh sửa lớp học",
                    "Xóa lớp học"
            };
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(c.getName() + " (" + c.getCode() + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showClassDetailsDialog(c);
                    } else if (which == 1) {
                        ClassScheduleBottomSheet.newInstance(c).show(getChildFragmentManager(), "ClassScheduleBottomSheet");
                    } else if (which == 2) {
                        ExamResultsBottomSheet.newInstanceForClass(c).show(getChildFragmentManager(), "ExamResultsBottomSheet");
                    } else if (which == 3) {
                        AttendanceBottomSheet.newInstance(c).show(getChildFragmentManager(), "AttendanceBottomSheet");
                    } else if (which == 4) {
                        EnrollStudentBottomSheet.newInstance(c).show(getChildFragmentManager(), "EnrollStudentBottomSheet");
                    } else if (which == 5) {
                        ClassBottomSheet.newInstance(c).show(getChildFragmentManager(), "ClassBottomSheet");
                    } else if (which == 6) {
                        confirmDeleteClass(c);
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showClassDetailsDialog(ClassRoom c) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(c.getName() + " (" + c.getCode() + ")")
                .setMessage("• Môn học: " + c.getSubjectName()
                        + "\n• Giáo viên phụ trách: " + c.getTeacherName()
                        + "\n• Phòng học: " + c.getRoomName()
                        + "\n• Sĩ số hiện tại: " + c.getCurrentStudents() + " / " + c.getMaxStudents() + " học sinh"
                        + "\n• Học phí: " + CurrencyUtils.formatVnd(c.getTuitionFee()) + " / tháng"
                        + "\n• Thời gian: " + DateUtils.toDisplayDate(c.getStartDate()) + " đến " + DateUtils.toDisplayDate(c.getEndDate())
                        + "\n• Trạng thái: " + c.getStatus()
                        + (c.isFull() ? "\n\n⚠️ Lớp học đã đạt sĩ số tối đa, không thể xếp thêm!" : ""))
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void confirmDeleteClass(ClassRoom c) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận xóa lớp học")
                .setMessage("Bạn có chắc chắn muốn xóa lớp " + c.getName() + " (" + c.getCode() + ") không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    classViewModel.deleteClass(c.getId());
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
