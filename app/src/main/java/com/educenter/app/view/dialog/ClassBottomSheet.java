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
import com.educenter.app.databinding.DialogClassBottomSheetBinding;
import com.educenter.app.model.ClassRoom;
import com.educenter.app.model.Room;
import com.educenter.app.model.Subject;
import com.educenter.app.model.Teacher;
import com.educenter.app.repository.ClassRepository;
import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.SubjectRepository;
import com.educenter.app.repository.TeacherRepository;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.utils.Validator;
import com.educenter.app.viewmodel.ClassViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * BottomSheet Dialog for creating or editing a Class record.
 */
public class ClassBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CLASS = "arg_class";

    private DialogClassBottomSheetBinding binding;
    private ClassViewModel classViewModel;
    private ClassRoom existingClass;

    private final List<Subject> subjectList = new ArrayList<>();
    private final List<Teacher> teacherList = new ArrayList<>();
    private final List<Room> roomList = new ArrayList<>();

    private long selectedSubjectId = -1;
    private long selectedTeacherId = -1;
    private long selectedRoomId = -1;

    public static ClassBottomSheet newInstance(@Nullable ClassRoom classRoom) {
        ClassBottomSheet sheet = new ClassBottomSheet();
        if (classRoom != null) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_CLASS, classRoom);
            sheet.setArguments(args);
        }
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingClass = (ClassRoom) getArguments().getSerializable(ARG_CLASS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogClassBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        classViewModel = new ViewModelProvider(requireActivity()).get(ClassViewModel.class);

        setupDatePickers();
        setupStatusDropdown();
        loadReferenceData();

        binding.btnClassSheetClose.setOnClickListener(v -> dismiss());
        binding.btnSaveClass.setOnClickListener(v -> saveClass());
    }

    private void setupDatePickers() {
        binding.etStartDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String formatted = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                binding.etStartDate.setText(formatted);
                binding.tilStartDate.setError(null);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        binding.etEndDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 3);
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String formatted = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                binding.etEndDate.setText(formatted);
                binding.tilEndDate.setError(null);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupStatusDropdown() {
        String[] statuses = {Constants.STATUS_CLASS_OPEN, Constants.STATUS_CLASS_UPCOMING, Constants.STATUS_CLASS_FINISHED};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses);
        binding.actStatus.setAdapter(statusAdapter);

        if (existingClass == null) {
            binding.actStatus.setText(Constants.STATUS_CLASS_OPEN, false);
            binding.etStartDate.setText(DateUtils.toDisplayDate(DateUtils.getTodayDb()));
            Calendar endCal = Calendar.getInstance();
            endCal.add(Calendar.MONTH, 3);
            String endFormatted = String.format(Locale.US, "%02d/%02d/%04d",
                    endCal.get(Calendar.DAY_OF_MONTH), endCal.get(Calendar.MONTH) + 1, endCal.get(Calendar.YEAR));
            binding.etEndDate.setText(endFormatted);
        }
    }

    private void loadReferenceData() {
        SubjectRepository subjectRepo = new SubjectRepository(requireContext());
        TeacherRepository teacherRepo = new TeacherRepository(requireContext());
        ClassRepository classRepo = new ClassRepository(requireContext());

        // Load subjects
        subjectRepo.getAllSubjects(new DataCallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> result) {
                subjectList.clear();
                subjectList.addAll(result);
                List<String> names = new ArrayList<>();
                for (Subject s : result) names.add(s.getName() + " (" + s.getCode() + ")");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
                binding.actSubject.setAdapter(adapter);

                binding.actSubject.setOnItemClickListener((parent, view, position, id) -> {
                    selectedSubjectId = subjectList.get(position).getId();
                });

                if (existingClass != null) {
                    for (int i = 0; i < subjectList.size(); i++) {
                        if (subjectList.get(i).getId() == existingClass.getSubjectId()) {
                            binding.actSubject.setText(names.get(i), false);
                            selectedSubjectId = existingClass.getSubjectId();
                            break;
                        }
                    }
                } else if (!subjectList.isEmpty()) {
                    binding.actSubject.setText(names.get(0), false);
                    selectedSubjectId = subjectList.get(0).getId();
                }
            }

            @Override
            public void onError(String message) {}
        });

        // Load teachers
        teacherRepo.getAllTeachers(new DataCallback<List<Teacher>>() {
            @Override
            public void onSuccess(List<Teacher> result) {
                teacherList.clear();
                teacherList.addAll(result);
                List<String> names = new ArrayList<>();
                for (Teacher t : result) names.add(t.getFullName() + " - " + t.getSpecialization());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
                binding.actTeacher.setAdapter(adapter);

                binding.actTeacher.setOnItemClickListener((parent, view, position, id) -> {
                    selectedTeacherId = teacherList.get(position).getId();
                });

                if (existingClass != null) {
                    for (int i = 0; i < teacherList.size(); i++) {
                        if (teacherList.get(i).getId() == existingClass.getTeacherId()) {
                            binding.actTeacher.setText(names.get(i), false);
                            selectedTeacherId = existingClass.getTeacherId();
                            break;
                        }
                    }
                } else if (!teacherList.isEmpty()) {
                    binding.actTeacher.setText(names.get(0), false);
                    selectedTeacherId = teacherList.get(0).getId();
                }
            }

            @Override
            public void onError(String message) {}
        });

        // Load rooms
        classRepo.getAllRooms(new DataCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> result) {
                roomList.clear();
                roomList.addAll(result);
                List<String> names = new ArrayList<>();
                for (Room r : result) names.add(r.getName() + " (Chứa tối đa: " + r.getCapacity() + " chỗ)");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
                binding.actRoom.setAdapter(adapter);

                binding.actRoom.setOnItemClickListener((parent, view, position, id) -> {
                    selectedRoomId = roomList.get(position).getId();
                });

                if (existingClass != null) {
                    for (int i = 0; i < roomList.size(); i++) {
                        if (roomList.get(i).getId() == existingClass.getRoomId()) {
                            binding.actRoom.setText(names.get(i), false);
                            selectedRoomId = existingClass.getRoomId();
                            break;
                        }
                    }
                } else if (!roomList.isEmpty()) {
                    binding.actRoom.setText(names.get(0), false);
                    selectedRoomId = roomList.get(0).getId();
                }
            }

            @Override
            public void onError(String message) {}
        });

        if (existingClass != null) {
            populateExistingData();
        }
    }

    private void populateExistingData() {
        binding.tvClassSheetTitle.setText("Chỉnh sửa lớp học");
        binding.btnSaveClass.setText("Cập nhật lớp học");

        binding.etClassCode.setText(existingClass.getCode());
        binding.etClassCode.setEnabled(false);

        binding.etClassName.setText(existingClass.getName());
        binding.etMaxStudents.setText(String.valueOf(existingClass.getMaxStudents()));
        binding.etTuitionFee.setText(String.valueOf((long) existingClass.getTuitionFee()));
        binding.etStartDate.setText(DateUtils.toDisplayDate(existingClass.getStartDate()));
        binding.etEndDate.setText(DateUtils.toDisplayDate(existingClass.getEndDate()));
        binding.actStatus.setText(existingClass.getStatus(), false);
    }

    private void saveClass() {
        String code = binding.etClassCode.getText() != null ? binding.etClassCode.getText().toString().trim().toUpperCase() : "";
        String name = binding.etClassName.getText() != null ? binding.etClassName.getText().toString().trim() : "";
        String maxStr = binding.etMaxStudents.getText() != null ? binding.etMaxStudents.getText().toString().trim() : "";
        String feeStr = binding.etTuitionFee.getText() != null ? binding.etTuitionFee.getText().toString().trim() : "";
        String startDisplay = binding.etStartDate.getText() != null ? binding.etStartDate.getText().toString().trim() : "";
        String endDisplay = binding.etEndDate.getText() != null ? binding.etEndDate.getText().toString().trim() : "";
        String status = binding.actStatus.getText() != null ? binding.actStatus.getText().toString().trim() : Constants.STATUS_CLASS_OPEN;

        boolean hasError = false;

        if (!Validator.isNotEmpty(code)) {
            binding.tilClassCode.setError("Vui lòng nhập mã lớp học!");
            hasError = true;
        } else {
            binding.tilClassCode.setError(null);
        }

        if (!Validator.isNotEmpty(name)) {
            binding.tilClassName.setError("Vui lòng nhập tên lớp học!");
            hasError = true;
        } else {
            binding.tilClassName.setError(null);
        }

        int maxStudents = 0;
        try {
            maxStudents = Integer.parseInt(maxStr);
            if (maxStudents <= 0) {
                binding.tilMaxStudents.setError("Sĩ số phải lớn hơn 0!");
                hasError = true;
            } else {
                binding.tilMaxStudents.setError(null);
            }
        } catch (NumberFormatException e) {
            binding.tilMaxStudents.setError("Sĩ số không hợp lệ!");
            hasError = true;
        }

        double tuitionFee = 0;
        try {
            tuitionFee = Double.parseDouble(feeStr);
            if (tuitionFee < 0) {
                binding.tilTuitionFee.setError("Học phí không thể âm!");
                hasError = true;
            } else {
                binding.tilTuitionFee.setError(null);
            }
        } catch (NumberFormatException e) {
            binding.tilTuitionFee.setError("Học phí không hợp lệ!");
            hasError = true;
        }

        if (!Validator.isNotEmpty(startDisplay)) {
            binding.tilStartDate.setError("Vui lòng chọn ngày bắt đầu!");
            hasError = true;
        } else {
            binding.tilStartDate.setError(null);
        }

        if (!Validator.isNotEmpty(endDisplay)) {
            binding.tilEndDate.setError("Vui lòng chọn ngày kết thúc!");
            hasError = true;
        } else {
            binding.tilEndDate.setError(null);
        }

        String startDb = DateUtils.toDbDate(startDisplay);
        String endDb = DateUtils.toDbDate(endDisplay);

        // TC16: Ngày kết thúc phải sau ngày bắt đầu
        if (!DateUtils.isEndDateAfterStartDate(startDb, endDb)) {
            binding.tilEndDate.setError("Ngày kết thúc phải sau ngày bắt đầu!");
            hasError = true;
        }

        if (selectedSubjectId <= 0) {
            binding.tilSubject.setError("Vui lòng chọn môn học!");
            hasError = true;
        } else {
            binding.tilSubject.setError(null);
        }

        if (selectedTeacherId <= 0) {
            binding.tilTeacher.setError("Vui lòng chọn giáo viên phụ trách!");
            hasError = true;
        } else {
            binding.tilTeacher.setError(null);
        }

        if (selectedRoomId <= 0) {
            binding.tilRoom.setError("Vui lòng chọn phòng học!");
            hasError = true;
        } else {
            binding.tilRoom.setError(null);
        }

        if (hasError) return;

        if (existingClass == null) {
            ClassRoom newClass = new ClassRoom(0, code, name, selectedSubjectId, selectedTeacherId,
                    selectedRoomId, maxStudents, tuitionFee, startDb, endDb, status);
            classViewModel.insertClass(newClass);
            Toast.makeText(requireContext(), "Tạo lớp học " + code + " thành công!", Toast.LENGTH_SHORT).show();
        } else {
            existingClass.setName(name);
            existingClass.setSubjectId(selectedSubjectId);
            existingClass.setTeacherId(selectedTeacherId);
            existingClass.setRoomId(selectedRoomId);
            existingClass.setMaxStudents(maxStudents);
            existingClass.setTuitionFee(tuitionFee);
            existingClass.setStartDate(startDb);
            existingClass.setEndDate(endDb);
            existingClass.setStatus(status);

            classViewModel.updateClass(existingClass);
            Toast.makeText(requireContext(), "Cập nhật lớp học thành công!", Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
