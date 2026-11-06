package com.educenter.app.view.dialog;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.educenter.app.R;
import com.educenter.app.databinding.DialogStudentBottomSheetBinding;
import com.educenter.app.model.Student;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.utils.Validator;
import com.educenter.app.viewmodel.StudentViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

/**
 * BottomSheet Dialog for creating or updating a Student record.
 */
public class StudentBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STUDENT = "arg_student";

    private DialogStudentBottomSheetBinding binding;
    private StudentViewModel studentViewModel;
    private Student existingStudent;

    public static StudentBottomSheet newInstance(@Nullable Student student) {
        StudentBottomSheet sheet = new StudentBottomSheet();
        if (student != null) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_STUDENT, student);
            sheet.setArguments(args);
        }
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingStudent = (Student) getArguments().getSerializable(ARG_STUDENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogStudentBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        studentViewModel = new ViewModelProvider(requireActivity()).get(StudentViewModel.class);

        setupDropdowns();
        setupDatePicker();

        if (existingStudent != null) {
            populateExistingData();
        }

        binding.btnSheetClose.setOnClickListener(v -> dismiss());
        binding.btnSaveStudent.setOnClickListener(v -> saveStudent());
    }

    private void setupDropdowns() {
        String[] grades = {"9", "10", "11", "12"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, grades);
        binding.actGradeLevel.setAdapter(gradeAdapter);

        String[] genders = {"Nam", "Nữ"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, genders);
        binding.actGender.setAdapter(genderAdapter);

        if (existingStudent == null) {
            binding.actGradeLevel.setText("12", false);
            binding.actGender.setText("Nam", false);
        }
    }

    private void setupDatePicker() {
        binding.etDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            // Default 17 years ago
            cal.add(Calendar.YEAR, -17);

            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String formatted = String.format(java.util.Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                binding.etDob.setText(formatted);
                binding.tilDob.setError(null);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void populateExistingData() {
        binding.tvSheetTitle.setText("Chỉnh sửa học sinh");
        binding.btnSaveStudent.setText("Cập nhật thông tin");

        binding.etFullName.setText(existingStudent.getFullName());
        binding.etStudentCode.setText(existingStudent.getStudentCode());
        binding.etStudentCode.setEnabled(false); // Code cannot be changed once created

        binding.actGradeLevel.setText(existingStudent.getGradeLevel(), false);
        binding.etDob.setText(DateUtils.toDisplayDate(existingStudent.getDateOfBirth()));
        binding.actGender.setText(existingStudent.getGender(), false);
        binding.etPhone.setText(existingStudent.getPhone());
        binding.etSchool.setText(existingStudent.getSchool());
        binding.etParentName.setText(existingStudent.getParentName());
        binding.etParentPhone.setText(existingStudent.getParentPhone());
        binding.etAddress.setText(existingStudent.getAddress());
        binding.switchActive.setChecked(existingStudent.isActive());
        binding.switchActive.setText(existingStudent.isActive() ? "Trạng thái: Đang theo học" : "Trạng thái: Đã nghỉ học");

        binding.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.switchActive.setText(isChecked ? "Trạng thái: Đang theo học" : "Trạng thái: Đã nghỉ học");
        });
    }

    private void saveStudent() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String studentCode = binding.etStudentCode.getText() != null ? binding.etStudentCode.getText().toString().trim().toUpperCase() : "";
        String grade = binding.actGradeLevel.getText() != null ? binding.actGradeLevel.getText().toString().trim() : "12";
        String dobDisplay = binding.etDob.getText() != null ? binding.etDob.getText().toString().trim() : "";
        String gender = binding.actGender.getText() != null ? binding.actGender.getText().toString().trim() : "Nam";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String school = binding.etSchool.getText() != null ? binding.etSchool.getText().toString().trim() : "";
        String parentName = binding.etParentName.getText() != null ? binding.etParentName.getText().toString().trim() : "";
        String parentPhone = binding.etParentPhone.getText() != null ? binding.etParentPhone.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "";
        boolean active = binding.switchActive.isChecked();

        // Validation
        boolean hasError = false;
        if (!Validator.isNotEmpty(fullName)) {
            binding.tilFullName.setError("Vui lòng nhập họ và tên học sinh!");
            hasError = true;
        } else {
            binding.tilFullName.setError(null);
        }

        if (!Validator.isNotEmpty(studentCode)) {
            binding.tilStudentCode.setError("Vui lòng nhập mã học sinh!");
            hasError = true;
        } else {
            binding.tilStudentCode.setError(null);
        }

        if (!Validator.isNotEmpty(dobDisplay)) {
            binding.tilDob.setError("Vui lòng chọn ngày sinh!");
            hasError = true;
        } else {
            binding.tilDob.setError(null);
        }

        if (!Validator.isNotEmpty(parentName)) {
            binding.tilParentName.setError("Vui lòng nhập tên phụ huynh!");
            hasError = true;
        } else {
            binding.tilParentName.setError(null);
        }

        if (!Validator.isValidPhone(parentPhone)) {
            binding.tilParentPhone.setError("Số điện thoại phụ huynh không hợp lệ (10 chữ số)!");
            hasError = true;
        } else {
            binding.tilParentPhone.setError(null);
        }

        if (hasError) {
            return;
        }

        String dobDb = DateUtils.toDbDate(dobDisplay);

        if (existingStudent == null) {
            // INSERT
            Student newStudent = new Student(0, studentCode, fullName, dobDb, gender, phone,
                    parentName, parentPhone, school, grade, address, active ? 1 : 0, DateUtils.getTodayDb());
            studentViewModel.insertStudent(newStudent);
            Toast.makeText(requireContext(), "Thêm học sinh " + fullName + " thành công!", Toast.LENGTH_SHORT).show();
        } else {
            // UPDATE
            existingStudent.setFullName(fullName);
            existingStudent.setGradeLevel(grade);
            existingStudent.setDateOfBirth(dobDb);
            existingStudent.setGender(gender);
            existingStudent.setPhone(phone);
            existingStudent.setSchool(school);
            existingStudent.setParentName(parentName);
            existingStudent.setParentPhone(parentPhone);
            existingStudent.setAddress(address);
            existingStudent.setActive(active ? 1 : 0);

            studentViewModel.updateStudent(existingStudent);
            Toast.makeText(requireContext(), "Cập nhật học sinh thành công!", Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
