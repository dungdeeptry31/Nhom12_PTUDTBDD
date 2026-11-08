package com.educenter.app.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.educenter.app.R;
import com.educenter.app.data.local.SessionManager;
import com.educenter.app.databinding.DialogAddRoomBinding;
import com.educenter.app.databinding.DialogChangePasswordBinding;
import com.educenter.app.databinding.FragmentProfileBinding;
import com.educenter.app.model.Room;
import com.educenter.app.model.Subject;
import com.educenter.app.model.Teacher;
import com.educenter.app.repository.AuthRepository;
import com.educenter.app.repository.ClassRepository;
import com.educenter.app.repository.DataCallback;
import com.educenter.app.repository.SubjectRepository;
import com.educenter.app.repository.TeacherRepository;
import com.educenter.app.utils.Constants;
import com.educenter.app.view.activity.LoginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Profile Fragment displaying current active user information, role permissions,
 * and secure logout action.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());

        displayProfileInfo();

        if (sessionManager.isAdmin()) {
            binding.cardAdminTools.setVisibility(View.VISIBLE);
            binding.btnManageTeachers.setOnClickListener(v -> showTeacherManagementDialog());
            binding.btnManageSubjects.setOnClickListener(v -> showSubjectManagementDialog());
            binding.btnManageRooms.setOnClickListener(v -> showRoomManagementDialog());
        } else {
            binding.cardAdminTools.setVisibility(View.GONE);
        }

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void showTeacherManagementDialog() {
        TeacherRepository repo = new TeacherRepository(requireContext());
        repo.getAllTeachers(new DataCallback<List<Teacher>>() {
            @Override
            public void onSuccess(List<Teacher> teachers) {
                if (teachers.isEmpty()) {
                    Toast.makeText(requireContext(), "Chưa có giáo viên nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] items = new String[teachers.size()];
                for (int i = 0; i < teachers.size(); i++) {
                    Teacher t = teachers.get(i);
                    items[i] = t.getFullName() + " (" + t.getSpecialization() + ") - "
                            + (t.getActiveClassesCount() > 0 ? "Đang dạy " + t.getActiveClassesCount() + " lớp" : "Chưa có lớp");
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Danh sách Giáo viên (" + teachers.size() + ")")
                        .setItems(items, (dialog, which) -> {
                            Teacher selected = teachers.get(which);
                            showTeacherActionDialog(selected, repo);
                        })
                        .setNegativeButton("Đóng", null)
                        .show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTeacherActionDialog(Teacher teacher, TeacherRepository repo) {
        String[] actions = {"Xem thông tin chi tiết", "Xóa giáo viên (TC09/TC10)"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(teacher.getFullName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(teacher.getFullName())
                                .setMessage("• Chuyên môn: " + teacher.getSpecialization()
                                        + "\n• Học vị: " + teacher.getDegree()
                                        + "\n• Kinh nghiệm: " + teacher.getExperience() + " năm"
                                        + "\n• SĐT: " + teacher.getPhone()
                                        + "\n• Email: " + teacher.getEmail()
                                        + "\n• Số lớp đang phụ trách: " + teacher.getActiveClassesCount() + " lớp")
                                .setPositiveButton("Đóng", null)
                                .show();
                    } else if (which == 1) {
                        // Confirm delete
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Xác nhận xóa giáo viên")
                                .setMessage("Bạn có chắc chắn muốn xóa giáo viên " + teacher.getFullName() + " không?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    repo.deleteTeacher(teacher.getId(), new DataCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            Toast.makeText(requireContext(), "Xóa giáo viên thành công!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            new MaterialAlertDialogBuilder(requireContext())
                                                    .setTitle("Không thể xóa giáo viên")
                                                    .setMessage(message)
                                                    .setPositiveButton("Đồng ý", null)
                                                    .show();
                                        }
                                    });
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showSubjectManagementDialog() {
        SubjectRepository repo = new SubjectRepository(requireContext());
        repo.getAllSubjects(new DataCallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                if (subjects.isEmpty()) {
                    Toast.makeText(requireContext(), "Chưa có môn học nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] items = new String[subjects.size()];
                for (int i = 0; i < subjects.size(); i++) {
                    Subject s = subjects.get(i);
                    items[i] = s.getName() + " (" + s.getCode() + ") - "
                            + (s.getActiveClassesCount() > 0 ? "Đang mở " + s.getActiveClassesCount() + " lớp" : "Chưa mở lớp");
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Danh sách Môn học (" + subjects.size() + ")")
                        .setItems(items, (dialog, which) -> {
                            Subject selected = subjects.get(which);
                            showSubjectActionDialog(selected, repo);
                        })
                        .setNegativeButton("Đóng", null)
                        .show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSubjectActionDialog(Subject subject, SubjectRepository repo) {
        String[] actions = {"Xem thông tin môn học", "Xóa môn học (TC10/TC11)"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(subject.getName() + " (" + subject.getCode() + ")")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(subject.getName() + " (" + subject.getCode() + ")")
                                .setMessage("• Mã môn: " + subject.getCode()
                                        + "\n• Tên môn: " + subject.getName()
                                        + "\n• Mô tả: " + subject.getDescription()
                                        + "\n• Số lớp đang mở: " + subject.getActiveClassesCount() + " lớp")
                                .setPositiveButton("Đóng", null)
                                .show();
                    } else if (which == 1) {
                        // Confirm delete
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Xác nhận xóa môn học")
                                .setMessage("Bạn có chắc chắn muốn xóa môn " + subject.getName() + " (" + subject.getCode() + ") không?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    repo.deleteSubject(subject.getId(), new DataCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            Toast.makeText(requireContext(), "Xóa môn học thành công!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            new MaterialAlertDialogBuilder(requireContext())
                                                    .setTitle("Không thể xóa môn học")
                                                    .setMessage(message)
                                                    .setPositiveButton("Đồng ý", null)
                                                    .show();
                                        }
                                    });
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showRoomManagementDialog() {
        ClassRepository repo = new ClassRepository(requireContext());
        repo.getAllRooms(new DataCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                List<String> items = new ArrayList<>();
                items.add("➕ Thêm phòng học mới");
                for (Room r : rooms) {
                    items.add(r.getName() + " (" + r.getCapacity() + " chỗ ngồi)"
                            + (r.getLocation() != null && !r.getLocation().isEmpty() ? " - " + r.getLocation() : ""));
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Quản lý Phòng học (" + rooms.size() + ")")
                        .setItems(items.toArray(new String[0]), (dialog, which) -> {
                            if (which == 0) {
                                showAddRoomDialog(repo);
                            } else {
                                Room selected = rooms.get(which - 1);
                                showRoomActionDialog(selected, repo);
                            }
                        })
                        .setNegativeButton("Đóng", null)
                        .show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddRoomDialog(ClassRepository repo) {
        DialogAddRoomBinding dialogBinding = DialogAddRoomBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thêm phòng học mới")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = dialogBinding.etRoomName.getText() != null
                            ? dialogBinding.etRoomName.getText().toString().trim() : "";
                    String capStr = dialogBinding.etCapacity.getText() != null
                            ? dialogBinding.etCapacity.getText().toString().trim() : "";
                    String location = dialogBinding.etLocation.getText() != null
                            ? dialogBinding.etLocation.getText().toString().trim() : "";

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập tên phòng học!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int capacity;
                    try {
                        capacity = Integer.parseInt(capStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Sức chứa không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Room r = new Room(0, name, capacity, location, 1);
                    repo.insertRoom(r, new DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long id) {
                            Toast.makeText(requireContext(), "Thêm phòng học thành công!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Lỗi thêm phòng học")
                                    .setMessage(message)
                                    .setPositiveButton("Đồng ý", null)
                                    .show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRoomActionDialog(Room room, ClassRepository repo) {
        String[] options = {"Xóa phòng học"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(room.getName() + " (" + room.getCapacity() + " chỗ ngồi)")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Xác nhận xóa phòng học")
                                .setMessage("Bạn có chắc chắn muốn xóa " + room.getName() + " không?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    repo.deleteRoom(room.getId(), new DataCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            Toast.makeText(requireContext(), "Xóa phòng học thành công!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            new MaterialAlertDialogBuilder(requireContext())
                                                    .setTitle("Không thể xóa phòng học")
                                                    .setMessage(message)
                                                    .setPositiveButton("Đồng ý", null)
                                                    .show();
                                        }
                                    });
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showChangePasswordDialog() {
        DialogChangePasswordBinding dialogBinding = DialogChangePasswordBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Lưu mật khẩu", (dialog, which) -> {
                    String oldPass = dialogBinding.etOldPassword.getText() != null
                            ? dialogBinding.etOldPassword.getText().toString().trim() : "";
                    String newPass = dialogBinding.etNewPassword.getText() != null
                            ? dialogBinding.etNewPassword.getText().toString().trim() : "";
                    String confirmPass = dialogBinding.etConfirmPassword.getText() != null
                            ? dialogBinding.etConfirmPassword.getText().toString().trim() : "";

                    if (oldPass.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu hiện tại!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPass.length() < 6) {
                        Toast.makeText(requireContext(), "Mật khẩu mới phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(requireContext(), "Xác nhận mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthRepository authRepo = new AuthRepository(requireContext());
                    authRepo.changePassword(sessionManager.getUserId(), oldPass, newPass, new DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Đổi mật khẩu thất bại")
                                    .setMessage(message)
                                    .setPositiveButton("Đồng ý", null)
                                    .show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void displayProfileInfo() {
        String fullName = sessionManager.getFullName();
        String username = sessionManager.getUsername();
        String role = sessionManager.getUserRole();

        binding.tvProfileFullName.setText(fullName.isEmpty() ? "Người dùng EduCenter" : fullName);
        binding.tvProfileUsername.setText("@" + username);

        // Safe initial for avatar
        String trimmed = fullName != null ? fullName.trim() : "";
        if (!trimmed.isEmpty()) {
            int lastSpace = trimmed.lastIndexOf(' ');
            char initial = (lastSpace >= 0 && lastSpace < trimmed.length() - 1)
                    ? trimmed.charAt(lastSpace + 1)
                    : trimmed.charAt(0);
            binding.tvAvatarInitial.setText(String.valueOf(initial).toUpperCase());
        } else {
            binding.tvAvatarInitial.setText("U");
        }

        binding.tvProfileRoleBadge.setText(role);

        if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            binding.tvRoleDescription.setText("Toàn quyền quản trị: học sinh, giáo viên, phòng học, lớp học, tài chính và hệ thống.");
        } else if (Constants.ROLE_TEACHER.equalsIgnoreCase(role)) {
            binding.tvRoleDescription.setText("Quyền hạn giảng viên: xem lớp được phân công, điểm danh học sinh và chấm điểm.");
        } else {
            binding.tvRoleDescription.setText("Quyền hạn nhân viên: hỗ trợ quản lý học sinh và thu nộp học phí.");
        }
    }

    /**
     * TC23: Logout and clear session.
     * Flags ensure the Back button cannot return to Dashboard.
     */
    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
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
