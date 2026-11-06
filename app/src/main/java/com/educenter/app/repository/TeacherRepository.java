package com.educenter.app.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.educenter.app.EduCenterApplication;
import com.educenter.app.data.local.DatabaseHelper;
import com.educenter.app.model.ClassRoom;
import com.educenter.app.model.Teacher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for Teacher management and assignments.
 */
public class TeacherRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public TeacherRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getAllTeachers(DataCallback<List<Teacher>> callback) {
        searchTeachers("", null, callback);
    }

    public void searchTeachers(String keyword, Integer activeFilter, DataCallback<List<Teacher>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT t.*, ")
               .append("(SELECT COUNT(*) FROM ").append(DatabaseHelper.TABLE_CLASSES)
               .append(" c WHERE c.teacher_id = t.id AND c.status = 'OPEN') as active_classes_count ")
               .append("FROM ").append(DatabaseHelper.TABLE_TEACHERS).append(" t WHERE 1=1 ");

            List<String> args = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (t.full_name LIKE ? OR t.specialization LIKE ? OR t.phone LIKE ?) ");
                String kw = "%" + keyword.trim() + "%";
                args.add(kw);
                args.add(kw);
                args.add(kw);
            }

            if (activeFilter != null) {
                sql.append("AND t.active = ? ");
                args.add(String.valueOf(activeFilter));
            }

            sql.append("ORDER BY t.id DESC");

            Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
            List<Teacher> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    Teacher t = new Teacher(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                            cursor.getString(cursor.getColumnIndexOrThrow("email")),
                            cursor.getString(cursor.getColumnIndexOrThrow("specialization")),
                            cursor.getString(cursor.getColumnIndexOrThrow("degree")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("experience")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("active"))
                    );
                    t.setActiveClassesCount(cursor.getInt(cursor.getColumnIndexOrThrow("active_classes_count")));
                    list.add(t);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi truy vấn giáo viên: " + e.getMessage()));
            } finally {
                cursor.close();
            }
        });
    }

    public void getTeacherById(long id, DataCallback<Teacher> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT t.*, "
                    + "(SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASSES + " c WHERE c.teacher_id = t.id AND c.status = 'OPEN') as active_classes_count "
                    + "FROM " + DatabaseHelper.TABLE_TEACHERS + " t WHERE t.id = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            try {
                if (cursor.moveToFirst()) {
                    Teacher t = new Teacher(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                            cursor.getString(cursor.getColumnIndexOrThrow("email")),
                            cursor.getString(cursor.getColumnIndexOrThrow("specialization")),
                            cursor.getString(cursor.getColumnIndexOrThrow("degree")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("experience")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("active"))
                    );
                    t.setActiveClassesCount(cursor.getInt(cursor.getColumnIndexOrThrow("active_classes_count")));
                    mainHandler.post(() -> callback.onSuccess(t));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy giáo viên."));
                }
            } finally {
                cursor.close();
            }
        });
    }

    public void insertTeacher(Teacher teacher, DataCallback<Long> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // TC07: Check duplicate phone
            Cursor checkPhone = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEACHERS + " WHERE phone = ?",
                    new String[]{teacher.getPhone().trim()}
            );
            try {
                if (checkPhone.moveToFirst() && checkPhone.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Số điện thoại này đã được sử dụng bởi giáo viên khác!"));
                    return;
                }
            } finally {
                checkPhone.close();
            }

            // TC06: Check duplicate email
            Cursor checkEmail = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEACHERS + " WHERE email = ?",
                    new String[]{teacher.getEmail().trim()}
            );
            try {
                if (checkEmail.moveToFirst() && checkEmail.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Email này đã được sử dụng bởi giáo viên khác!"));
                    return;
                }
            } finally {
                checkEmail.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("full_name", teacher.getFullName().trim());
            cv.put("phone", teacher.getPhone().trim());
            cv.put("email", teacher.getEmail().trim());
            cv.put("specialization", teacher.getSpecialization());
            cv.put("degree", teacher.getDegree());
            cv.put("experience", teacher.getExperience());
            cv.put("active", teacher.getActive());

            long id = db.insert(DatabaseHelper.TABLE_TEACHERS, null, cv);
            if (id > 0) {
                mainHandler.post(() -> callback.onSuccess(id));
            } else {
                mainHandler.post(() -> callback.onError("Thêm giáo viên thất bại."));
            }
        });
    }

    public void updateTeacher(Teacher teacher, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check duplicate phone for other teachers
            Cursor checkPhone = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEACHERS + " WHERE phone = ? AND id != ?",
                    new String[]{teacher.getPhone().trim(), String.valueOf(teacher.getId())}
            );
            try {
                if (checkPhone.moveToFirst() && checkPhone.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Số điện thoại này đã được sử dụng!"));
                    return;
                }
            } finally {
                checkPhone.close();
            }

            // Check duplicate email for other teachers
            Cursor checkEmail = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEACHERS + " WHERE email = ? AND id != ?",
                    new String[]{teacher.getEmail().trim(), String.valueOf(teacher.getId())}
            );
            try {
                if (checkEmail.moveToFirst() && checkEmail.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Email này đã được sử dụng!"));
                    return;
                }
            } finally {
                checkEmail.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("full_name", teacher.getFullName().trim());
            cv.put("phone", teacher.getPhone().trim());
            cv.put("email", teacher.getEmail().trim());
            cv.put("specialization", teacher.getSpecialization());
            cv.put("degree", teacher.getDegree());
            cv.put("experience", teacher.getExperience());
            cv.put("active", teacher.getActive());

            int rows = db.update(DatabaseHelper.TABLE_TEACHERS, cv, "id = ?", new String[]{String.valueOf(teacher.getId())});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Cập nhật giáo viên thất bại."));
            }
        });
    }

    public void deleteTeacher(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // TC09: Check if teacher is assigned to any class
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASSES + " WHERE teacher_id = ?",
                    new String[]{String.valueOf(id)}
            );
            try {
                if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Không thể xóa giáo viên này vì đang được phân công giảng dạy lớp học."));
                    return;
                }
            } finally {
                cursor.close();
            }

            int rows = db.delete(DatabaseHelper.TABLE_TEACHERS, "id = ?", new String[]{String.valueOf(id)});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Xóa giáo viên thất bại."));
            }
        });
    }

    public void getAssignedClasses(long teacherId, DataCallback<List<ClassRoom>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT c.*, s.name as subject_name, t.full_name as teacher_name, r.name as room_name, "
                    + "(SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASS_STUDENTS + " cs WHERE cs.class_id = c.id AND cs.status = 'ACTIVE') as current_students "
                    + "FROM " + DatabaseHelper.TABLE_CLASSES + " c "
                    + "JOIN " + DatabaseHelper.TABLE_SUBJECTS + " s ON c.subject_id = s.id "
                    + "JOIN " + DatabaseHelper.TABLE_TEACHERS + " t ON c.teacher_id = t.id "
                    + "JOIN " + DatabaseHelper.TABLE_ROOMS + " r ON c.room_id = r.id "
                    + "WHERE c.teacher_id = ? "
                    + "ORDER BY c.id DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teacherId)});
            List<ClassRoom> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    ClassRoom c = new ClassRoom(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("code")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("subject_id")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("teacher_id")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("room_id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("max_students")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("tuition_fee")),
                            cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    );
                    c.setSubjectName(cursor.getString(cursor.getColumnIndexOrThrow("subject_name")));
                    c.setTeacherName(cursor.getString(cursor.getColumnIndexOrThrow("teacher_name")));
                    c.setRoomName(cursor.getString(cursor.getColumnIndexOrThrow("room_name")));
                    c.setCurrentStudents(cursor.getInt(cursor.getColumnIndexOrThrow("current_students")));
                    list.add(c);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } finally {
                cursor.close();
            }
        });
    }
}
