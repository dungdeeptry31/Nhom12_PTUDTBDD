package com.educenter.app.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.educenter.app.EduCenterApplication;
import com.educenter.app.data.local.DatabaseHelper;
import com.educenter.app.model.Attendance;
import com.educenter.app.model.ClassRoom;
import com.educenter.app.model.ExamResult;
import com.educenter.app.model.Student;
import com.educenter.app.model.TuitionInvoice;
import com.educenter.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for Student management and relational details.
 */
public class StudentRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public StudentRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getAllStudents(DataCallback<List<Student>> callback) {
        searchStudents("", "", null, callback);
    }

    public void searchStudents(String keyword, String gradeFilter, Integer activeFilter, DataCallback<List<Student>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT s.*, ")
               .append("(SELECT COUNT(*) FROM ").append(DatabaseHelper.TABLE_CLASS_STUDENTS)
               .append(" cs WHERE cs.student_id = s.id AND cs.status = 'ACTIVE') as enrolled_classes_count ")
               .append("FROM ").append(DatabaseHelper.TABLE_STUDENTS).append(" s WHERE 1=1 ");

            List<String> args = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (s.full_name LIKE ? OR s.student_code LIKE ? OR s.phone LIKE ?) ");
                String kw = "%" + keyword.trim() + "%";
                args.add(kw);
                args.add(kw);
                args.add(kw);
            }

            if (gradeFilter != null && !gradeFilter.trim().isEmpty() && !gradeFilter.equals("Tất cả")) {
                sql.append("AND s.grade_level = ? ");
                args.add(gradeFilter.trim());
            }

            if (activeFilter != null) {
                sql.append("AND s.active = ? ");
                args.add(String.valueOf(activeFilter));
            }

            sql.append("ORDER BY s.id DESC");

            Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
            List<Student> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    Student s = parseStudent(cursor);
                    s.setEnrolledClassesCount(cursor.getInt(cursor.getColumnIndexOrThrow("enrolled_classes_count")));
                    list.add(s);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi truy vấn học sinh: " + e.getMessage()));
            } finally {
                cursor.close();
            }
        });
    }

    public void getStudentById(long id, DataCallback<Student> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT s.*, "
                    + "(SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASS_STUDENTS + " cs WHERE cs.student_id = s.id AND cs.status = 'ACTIVE') as enrolled_classes_count "
                    + "FROM " + DatabaseHelper.TABLE_STUDENTS + " s WHERE s.id = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            try {
                if (cursor.moveToFirst()) {
                    Student s = parseStudent(cursor);
                    s.setEnrolledClassesCount(cursor.getInt(cursor.getColumnIndexOrThrow("enrolled_classes_count")));
                    mainHandler.post(() -> callback.onSuccess(s));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy học sinh."));
                }
            } finally {
                cursor.close();
            }
        });
    }

    public void insertStudent(Student student, DataCallback<Long> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check duplicate student_code
            Cursor check = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_STUDENTS + " WHERE student_code = ?",
                    new String[]{student.getStudentCode().trim()}
            );
            try {
                if (check.moveToFirst() && check.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Mã học sinh đã tồn tại!"));
                    return;
                }
            } finally {
                check.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("student_code", student.getStudentCode().trim());
            cv.put("full_name", student.getFullName().trim());
            cv.put("date_of_birth", student.getDateOfBirth());
            cv.put("gender", student.getGender());
            cv.put("phone", student.getPhone());
            cv.put("parent_name", student.getParentName());
            cv.put("parent_phone", student.getParentPhone());
            cv.put("school", student.getSchool());
            cv.put("grade_level", student.getGradeLevel());
            cv.put("address", student.getAddress());
            cv.put("active", student.getActive());
            cv.put("created_at", DateUtils.getCurrentDateTimeDb());

            long id = db.insert(DatabaseHelper.TABLE_STUDENTS, null, cv);
            if (id > 0) {
                mainHandler.post(() -> callback.onSuccess(id));
            } else {
                mainHandler.post(() -> callback.onError("Thêm học sinh thất bại."));
            }
        });
    }

    public void updateStudent(Student student, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check duplicate student_code for other students
            Cursor check = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_STUDENTS + " WHERE student_code = ? AND id != ?",
                    new String[]{student.getStudentCode().trim(), String.valueOf(student.getId())}
            );
            try {
                if (check.moveToFirst() && check.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Mã học sinh bị trùng với học sinh khác!"));
                    return;
                }
            } finally {
                check.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("student_code", student.getStudentCode().trim());
            cv.put("full_name", student.getFullName().trim());
            cv.put("date_of_birth", student.getDateOfBirth());
            cv.put("gender", student.getGender());
            cv.put("phone", student.getPhone());
            cv.put("parent_name", student.getParentName());
            cv.put("parent_phone", student.getParentPhone());
            cv.put("school", student.getSchool());
            cv.put("grade_level", student.getGradeLevel());
            cv.put("address", student.getAddress());
            cv.put("active", student.getActive());

            int rows = db.update(DatabaseHelper.TABLE_STUDENTS, cv, "id = ?", new String[]{String.valueOf(student.getId())});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Cập nhật thông tin thất bại."));
            }
        });
    }

    public void deleteStudent(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check references in class_students, invoices, attendance
            Cursor cursorInv = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TUITION_INVOICES + " WHERE student_id = ?", new String[]{String.valueOf(id)});
            int invCount = 0;
            if (cursorInv.moveToFirst()) invCount = cursorInv.getInt(0);
            cursorInv.close();

            Cursor cursorAtt = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ATTENDANCE + " WHERE student_id = ?", new String[]{String.valueOf(id)});
            int attCount = 0;
            if (cursorAtt.moveToFirst()) attCount = cursorAtt.getInt(0);
            cursorAtt.close();

            if (invCount > 0 || attCount > 0) {
                // Cannot hard delete, switch to inactive
                ContentValues cv = new ContentValues();
                cv.put("active", 0);
                db.update(DatabaseHelper.TABLE_STUDENTS, cv, "id = ?", new String[]{String.valueOf(id)});
                mainHandler.post(() -> callback.onError("Học sinh đã phát sinh hóa đơn hoặc điểm danh. Hệ thống đã chuyển trạng thái sang 'Ngừng hoạt động' để bảo toàn dữ liệu."));
                return;
            }

            // Safe to delete enrollments and student
            db.beginTransaction();
            try {
                db.delete(DatabaseHelper.TABLE_CLASS_STUDENTS, "student_id = ?", new String[]{String.valueOf(id)});
                db.delete(DatabaseHelper.TABLE_STUDENTS, "id = ?", new String[]{String.valueOf(id)});
                db.setTransactionSuccessful();
                mainHandler.post(() -> callback.onSuccess(true));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi khi xóa học sinh: " + e.getMessage()));
            } finally {
                db.endTransaction();
            }
        });
    }

    public void getEnrolledClassesForStudent(long studentId, DataCallback<List<ClassRoom>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT c.*, s.name as subject_name, t.full_name as teacher_name, r.name as room_name, "
                    + "(SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASS_STUDENTS + " cs2 WHERE cs2.class_id = c.id AND cs2.status = 'ACTIVE') as current_students "
                    + "FROM " + DatabaseHelper.TABLE_CLASSES + " c "
                    + "JOIN " + DatabaseHelper.TABLE_CLASS_STUDENTS + " cs ON c.id = cs.class_id "
                    + "JOIN " + DatabaseHelper.TABLE_SUBJECTS + " s ON c.subject_id = s.id "
                    + "JOIN " + DatabaseHelper.TABLE_TEACHERS + " t ON c.teacher_id = t.id "
                    + "JOIN " + DatabaseHelper.TABLE_ROOMS + " r ON c.room_id = r.id "
                    + "WHERE cs.student_id = ? AND cs.status = 'ACTIVE'";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});
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

    private Student parseStudent(Cursor cursor) {
        return new Student(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("student_code")),
                cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                cursor.getString(cursor.getColumnIndexOrThrow("parent_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("parent_phone")),
                cursor.getString(cursor.getColumnIndexOrThrow("school")),
                cursor.getString(cursor.getColumnIndexOrThrow("grade_level")),
                cursor.getString(cursor.getColumnIndexOrThrow("address")),
                cursor.getInt(cursor.getColumnIndexOrThrow("active")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
        );
    }
}
