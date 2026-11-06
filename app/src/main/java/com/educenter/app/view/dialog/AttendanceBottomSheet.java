package com.educenter.app.view.dialog;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.educenter.app.adapter.AttendanceAdapter;
import com.educenter.app.databinding.DialogAttendanceSheetBinding;
import com.educenter.app.model.Attendance;
import com.educenter.app.model.ClassRoom;
import com.educenter.app.model.Student;
import com.educenter.app.repository.ClassRepository;
import com.educenter.app.repository.DataCallback;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.viewmodel.AttendanceViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * BottomSheet Dialog for conducting and managing session attendance.
 */
public class AttendanceBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CLASS = "arg_class";

    private DialogAttendanceSheetBinding binding;
    private AttendanceViewModel attendanceViewModel;
    private ClassRoom targetClass;
    private AttendanceAdapter adapter;
    private String selectedDateDb;

    public static AttendanceBottomSheet newInstance(ClassRoom classRoom) {
        AttendanceBottomSheet sheet = new AttendanceBottomSheet();
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
        selectedDateDb = DateUtils.getTodayDb();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAttendanceSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        attendanceViewModel = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);

        adapter = new AttendanceAdapter();
        binding.rvAttendanceList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAttendanceList.setAdapter(adapter);

        if (targetClass != null) {
            binding.tvAttendanceClassName.setText(targetClass.getName() + " (" + targetClass.getCode() + ")");
        }

        updateDateDisplay();

        binding.tvAttendanceDate.setOnClickListener(v -> showDatePicker());
        binding.btnAllPresent.setOnClickListener(v -> adapter.markAllPresent());
        binding.btnSaveAttendance.setOnClickListener(v -> saveAttendance());
        binding.btnAttendanceClose.setOnClickListener(v -> dismiss());

        loadAttendanceForSelectedDate();
    }

    private void updateDateDisplay() {
        binding.tvAttendanceDate.setText(DateUtils.toDisplayDate(selectedDateDb));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDateDb = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            updateDateDisplay();
            loadAttendanceForSelectedDate();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadAttendanceForSelectedDate() {
        if (targetClass == null) return;

        attendanceViewModel.loadAttendance(targetClass.getId(), selectedDateDb);
        attendanceViewModel.getAttendanceListLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                adapter.setList(list);
            } else {
                // If no record exists yet, fetch students enrolled in class and default to PRESENT
                ClassRepository classRepo = new ClassRepository(requireContext());
                classRepo.getStudentsInClass(targetClass.getId(), new DataCallback<List<Student>>() {
                    @Override
                    public void onSuccess(List<Student> students) {
                        List<Attendance> freshList = new ArrayList<>();
                        for (Student s : students) {
                            Attendance att = new Attendance(0, targetClass.getId(), s.getId(),
                                    selectedDateDb, Constants.ATTENDANCE_PRESENT, "");
                            att.setStudentName(s.getFullName());
                            att.setStudentCode(s.getStudentCode());
                            freshList.add(att);
                        }
                        adapter.setList(freshList);
                    }

                    @Override
                    public void onError(String message) {}
                });
            }
        });
    }

    private void saveAttendance() {
        if (targetClass == null) return;
        List<Attendance> list = adapter.getList();
        if (list.isEmpty()) {
            Toast.makeText(requireContext(), "Lớp học chưa có học sinh nào để điểm danh!", Toast.LENGTH_SHORT).show();
            return;
        }

        attendanceViewModel.saveAttendanceBatch(targetClass.getId(), selectedDateDb, list);
        Toast.makeText(requireContext(), "Lưu điểm danh ngày " + DateUtils.toDisplayDate(selectedDateDb) + " thành công!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
