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
import com.educenter.app.model.Room;
import com.educenter.app.model.Student;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for Class management, Student enrollment, and capacity enforcement.
 */
public class ClassRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public ClassRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getAllClasses(DataCallback<List<ClassRoom>> callback) {
        searchClasses("", null, null, null, callback);
    }

    public void searchClasses(String keyword, Long subjectId, Long teacherId, String status, DataCallback<List<ClassRoom>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT c.*, s.name as subject_name, t.full_name as teacher_name, r.name as room_name, ")
               .append("(SELECT COUNT(*) FROM ").append(DatabaseHelper.TABLE_CLASS_STUDENTS)
               .append(" cs WHERE cs.class_id = c.id AND cs.status = 'ACTIVE') as current_students ")
               .append("FROM ").append(DatabaseHelper.TABLE_CLASSES).append(" c ")
               .append("JOIN ").append(DatabaseHelper.TABLE_SUBJECTS).append(" s ON c.subject_id = s.id ")
               .append("JOIN ").append(DatabaseHelper.TABLE_TEACHERS).append(" t ON c.teacher_id = t.id ")
               .append("JOIN ").append(DatabaseHelper.TABLE_ROOMS).append(" r ON c.room_id = r.id ")
               .append("WHERE 1=1 ");

            List<String> args = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (c.name LIKE ? OR c.code LIKE ?) ");
                String kw = "%" + keyword.trim() + "%";
                args.add(kw);
                args.add(kw);
            }

            if (subjectId != null && subjectId > 0) {
                sql.append("AND c.subject_id = ? ");
                args.add(String.valueOf(subjectId));
            }

            if (teacherId != null && teacherId > 0) {
                sql.append("AND c.teacher_id = ? ");
                args.add(String.valueOf(teacherId));
            }

            if (status != null && !status.trim().isEmpty() && !status.equals("Tất cả")) {
                sql.append("AND c.status = ? ");
                args.add(status.trim());
            }

            sql.append("ORDER BY c.id DESC");

            Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
            List<ClassRoom> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    ClassRoom c = parseClass(cursor);
                    list.add(c);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi truy vấn lớp học: " + e.getMessage()));
            } finally {
                cursor.close();
            }
        });
    }

    public void getClassById(long id, DataCallback<ClassRoom> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT c.*, s.name as subject_name, t.full_name as teacher_name, r.name as room_name, "
                    + "(SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASS_STUDENTS + " cs WHERE cs.class_id = c.id AND cs.status = 'ACTIVE') as current_students "
                    + "FROM " + DatabaseHelper.TABLE_CLASSES + " c "
                    + "JOIN " + DatabaseHelper.TABLE_SUBJECTS + " s ON c.subject_id = s.id "
                    + "JOIN " + DatabaseHelper.TABLE_TEACHERS + " t ON c.teacher_id = t.id "
                    + "JOIN " + DatabaseHelper.TABLE_ROOMS + " r ON c.room_id = r.id "
                    + "WHERE c.id = ?";

            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            try {
                if (cursor.moveToFirst()) {
                    ClassRoom c = parseClass(cursor);
                    mainHandler.post(() -> callback.onSuccess(c));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy lớp học."));
                }
            } finally {
                cursor.close();
            }
        });
    }

    public void insertClass(ClassRoom classRoom, DataCallback<Long> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check duplicate code
            Cursor checkCode = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASSES + " WHERE code = ?",
                    new String[]{classRoom.getCode().trim()}
            );
            try {
                if (checkCode.moveToFirst() && checkCode.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Mã lớp học đã tồn tại!"));
                    return;
                }
            } finally {
                checkCode.close();
            }

            // Check room capacity
            Cursor roomCursor = db.rawQuery(
                    "SELECT capacity FROM " + DatabaseHelper.TABLE_ROOMS + " WHERE id = ?",
                    new String[]{String.valueOf(classRoom.getRoomId())}
            );
            try {
                if (roomCursor.moveToFirst()) {
                    int roomCapacity = roomCursor.getInt(0);
                    if (classRoom.getMaxStudents() > roomCapacity) {
                        mainHandler.post(() -> callback.onError("Sĩ số tối đa (" + classRoom.getMaxStudents()
                                + ") không được vượt quá sức chứa của phòng học (" + roomCapacity + " chỗ)!"));
                        return;
                    }
                }
            } finally {
                roomCursor.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("code", classRoom.getCode().trim().toUpperCase());
            cv.put("name", classRoom.getName().trim());
            cv.put("subject_id", classRoom.getSubjectId());
            cv.put("teacher_id", classRoom.getTeacherId());
            cv.put("room_id", classRoom.getRoomId());
            cv.put("max_students", classRoom.getMaxStudents());
            cv.put("tuition_fee", classRoom.getTuitionFee());
            cv.put("start_date", classRoom.getStartDate());
            cv.put("end_date", classRoom.getEndDate());
            cv.put("status", classRoom.getStatus());

            long id = db.insert(DatabaseHelper.TABLE_CLASSES, null, cv);
            if (id > 0) {
                mainHandler.post(() -> callback.onSuccess(id));
            } else {
                mainHandler.post(() -> callback.onError("Thêm lớp học thất bại."));
            }
        });
    }

    public void updateClass(ClassRoom classRoom, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check room capacity
            Cursor roomCursor = db.rawQuery(
                    "SELECT capacity FROM " + DatabaseHelper.TABLE_ROOMS + " WHERE id = ?",
                    new String[]{String.valueOf(classRoom.getRoomId())}
            );
            try {
                if (roomCursor.moveToFirst()) {
                    int roomCapacity = roomCursor.getInt(0);
                    if (classRoom.getMaxStudents() > roomCapacity) {
                        mainHandler.post(() -> callback.onError("Sĩ số tối đa vượt quá sức chứa của phòng học!"));
                        return;
                    }
                }
            } finally {
                roomCursor.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("code", classRoom.getCode().trim().toUpperCase());
            cv.put("name", classRoom.getName().trim());
            cv.put("subject_id", classRoom.getSubjectId());
            cv.put("teacher_id", classRoom.getTeacherId());
            cv.put("room_id", classRoom.getRoomId());
            cv.put("max_students", classRoom.getMaxStudents());
            cv.put("tuition_fee", classRoom.getTuitionFee());
            cv.put("start_date", classRoom.getStartDate());
            cv.put("end_date", classRoom.getEndDate());
            cv.put("status", classRoom.getStatus());

            int rows = db.update(DatabaseHelper.TABLE_CLASSES, cv, "id = ?", new String[]{String.valueOf(classRoom.getId())});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Cập nhật lớp học thất bại."));
            }
        });
    }

    public void deleteClass(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check if class has enrollments, attendance, or invoices
            Cursor cursorCS = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASS_STUDENTS + " WHERE class_id = ?", new String[]{String.valueOf(id)});
            int csCount = 0;
            if (cursorCS.moveToFirst()) csCount = cursorCS.getInt(0);
            cursorCS.close();

            if (csCount > 0) {
                mainHandler.post(() -> callback.onError("Không thể xóa lớp học này vì đã có học sinh đăng ký tham gia!"));
                return;
            }

            int rows = db.delete(DatabaseHelper.TABLE_CLASSES, "id = ?", new String[]{String.valueOf(id)});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Xóa lớp học thất bại."));
            }
        });
    }

    /**
     * Enrolls a student into a class.
     * Enforces TC12 (success if spots open), TC13 (block if class full), and TC14 (block if duplicate).
     */
    public void enrollStudent(long classId, long studentId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                // 1. Check if class exists and status
                Cursor classCursor = db.rawQuery(
                        "SELECT max_students, status FROM " + DatabaseHelper.TABLE_CLASSES + " WHERE id = ?",
                        new String[]{String.valueOf(classId)}
                );
                int maxStudents = 0;
                String status = "";
                if (classCursor.moveToFirst()) {
                    maxStudents = classCursor.getInt(0);
                    status = classCursor.getString(1);
                } else {
                    classCursor.close();
                    mainHandler.post(() -> callback.onError("Lớp học không tồn tại!"));
                    return;
                }
                classCursor.close();

                if (Constants.STATUS_CLASS_FINISHED.equalsIgnoreCase(status)) {
                    mainHandler.post(() -> callback.onError("Lớp học đã kết thúc, không thể xếp thêm học sinh!"));
                    return;
                }

                // 2. TC14: Check if student already enrolled in this class
                Cursor existCursor = db.rawQuery(
                        "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASS_STUDENTS + " WHERE class_id = ? AND student_id = ? AND status = 'ACTIVE'",
                        new String[]{String.valueOf(classId), String.valueOf(studentId)}
                );
                if (existCursor.moveToFirst() && existCursor.getInt(0) > 0) {
                    existCursor.close();
                    mainHandler.post(() -> callback.onError("Học sinh đã được đăng ký vào lớp này!"));
                    return;
                }
                existCursor.close();

                // 3. TC13: Count current active students & check capacity
                Cursor countCursor = db.rawQuery(
                        "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASS_STUDENTS + " WHERE class_id = ? AND status = 'ACTIVE'",
                        new String[]{String.valueOf(classId)}
                );
                int currentStudents = 0;
                if (countCursor.moveToFirst()) {
                    currentStudents = countCursor.getInt(0);
                }
                countCursor.close();

                if (currentStudents >= maxStudents) {
                    mainHandler.post(() -> callback.onError("Lớp học đã đạt sĩ số tối đa, không thể xếp thêm!"));
                    return;
                }

                // 4. Insert enrollment
                ContentValues cv = new ContentValues();
                cv.put("class_id", classId);
                cv.put("student_id", studentId);
                cv.put("enrolled_date", DateUtils.getTodayDb());
                cv.put("status", "ACTIVE");

                long result = db.insert(DatabaseHelper.TABLE_CLASS_STUDENTS, null, cv);
                if (result > 0) {
                    db.setTransactionSuccessful();
                    mainHandler.post(() -> callback.onSuccess(true)); // TC12
                } else {
                    mainHandler.post(() -> callback.onError("Đăng ký vào lớp thất bại."));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi khi xếp lớp: " + e.getMessage()));
            } finally {
                db.endTransaction();
            }
        });
    }

    public void unenrollStudent(long classId, long studentId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete(DatabaseHelper.TABLE_CLASS_STUDENTS, "class_id = ? AND student_id = ?",
                    new String[]{String.valueOf(classId), String.valueOf(studentId)});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Hủy đăng ký thất bại."));
            }
        });
    }

    public void getStudentsInClass(long classId, DataCallback<List<Student>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT s.* FROM " + DatabaseHelper.TABLE_STUDENTS + " s "
                    + "JOIN " + DatabaseHelper.TABLE_CLASS_STUDENTS + " cs ON s.id = cs.student_id "
                    + "WHERE cs.class_id = ? AND cs.status = 'ACTIVE' "
                    + "ORDER BY s.full_name ASC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(classId)});
            List<Student> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    Student s = new Student(
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
                    list.add(s);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } finally {
                cursor.close();
            }
        });
    }

    public void getAllRooms(DataCallback<List<Room>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_ROOMS + " WHERE active = 1 ORDER BY name ASC", null);
            List<Room> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    list.add(new Room(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("capacity")),
                            cursor.getString(cursor.getColumnIndexOrThrow("location")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("active"))
                    ));
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi lấy danh sách phòng: " + e.getMessage()));
            } finally {
                cursor.close();
            }
        });
    }

    private ClassRoom parseClass(Cursor cursor) {
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
        return c;
    }
}
