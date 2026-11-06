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
import com.educenter.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for Attendance management and Batch Attendance saving.
 */
public class AttendanceRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public AttendanceRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Retrieves attendance for a given class and date.
     * If no records exist yet for this date, automatically pre-fills enrolled students with default status PRESENT.
     */
    public void getAttendanceForSession(long classId, String date, DataCallback<List<Attendance>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Query enrolled students and join existing attendance for this date
            String sql = "SELECT s.id as student_id, s.full_name as student_name, s.student_code as student_code, "
                    + "att.id as attendance_id, att.status as attendance_status, att.note as attendance_note "
                    + "FROM " + DatabaseHelper.TABLE_STUDENTS + " s "
                    + "JOIN " + DatabaseHelper.TABLE_CLASS_STUDENTS + " cs ON s.id = cs.student_id "
                    + "LEFT JOIN " + DatabaseHelper.TABLE_ATTENDANCE + " att "
                    + "ON s.id = att.student_id AND att.class_id = ? AND att.attendance_date = ? "
                    + "WHERE cs.class_id = ? AND cs.status = 'ACTIVE' "
                    + "ORDER BY s.full_name ASC";

            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(classId), date, String.valueOf(classId)});
            List<Attendance> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    long attId = cursor.isNull(cursor.getColumnIndexOrThrow("attendance_id")) ? -1 : cursor.getLong(cursor.getColumnIndexOrThrow("attendance_id"));
                    long studentId = cursor.getLong(cursor.getColumnIndexOrThrow("student_id"));
                    String studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name"));
                    String studentCode = cursor.getString(cursor.getColumnIndexOrThrow("student_code"));
                    String status = cursor.isNull(cursor.getColumnIndexOrThrow("attendance_status"))
                            ? Constants.ATTENDANCE_PRESENT
                            : cursor.getString(cursor.getColumnIndexOrThrow("attendance_status"));
                    String note = cursor.isNull(cursor.getColumnIndexOrThrow("attendance_note"))
                            ? ""
                            : cursor.getString(cursor.getColumnIndexOrThrow("attendance_note"));

                    Attendance a = new Attendance(attId, classId, studentId, date, status, note);
                    a.setStudentName(studentName);
                    a.setStudentCode(studentCode);
                    list.add(a);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi khi tải danh sách điểm danh: " + e.getMessage()));
            } finally {
                cursor.close();
            }
        });
    }

    /**
     * Batch saves attendance records using an atomic SQLite transaction.
     * Handles TC17 / TC18 (updates existing records seamlessly).
     */
    public void saveAttendanceBatch(long classId, String date, List<Attendance> attendanceList, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Attendance att : attendanceList) {
                    ContentValues cv = new ContentValues();
                    cv.put("class_id", classId);
                    cv.put("student_id", att.getStudentId());
                    cv.put("attendance_date", date);
                    cv.put("status", att.getStatus());
                    cv.put("note", att.getNote() != null ? att.getNote() : "");

                    // Insert or replace on UNIQUE(class_id, student_id, attendance_date)
                    db.insertWithOnConflict(
                            DatabaseHelper.TABLE_ATTENDANCE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_REPLACE
                    );
                }
                db.setTransactionSuccessful();
                mainHandler.post(() -> callback.onSuccess(true));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi khi lưu điểm danh: " + e.getMessage()));
            } finally {
                db.endTransaction();
            }
        });
    }

    public void getAttendanceStatsForClass(long classId, DataCallback<int[]> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT status, COUNT(*) as count FROM " + DatabaseHelper.TABLE_ATTENDANCE
                    + " WHERE class_id = ? GROUP BY status";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(classId)});
            int[] stats = new int[3]; // [present, absent, late]
            try {
                while (cursor.moveToNext()) {
                    String status = cursor.getString(0);
                    int count = cursor.getInt(1);
                    if (Constants.ATTENDANCE_PRESENT.equalsIgnoreCase(status)) {
                        stats[0] = count;
                    } else if (Constants.ATTENDANCE_ABSENT.equalsIgnoreCase(status)) {
                        stats[1] = count;
                    } else if (Constants.ATTENDANCE_LATE.equalsIgnoreCase(status)) {
                        stats[2] = count;
                    }
                }
                mainHandler.post(() -> callback.onSuccess(stats));
            } finally {
                cursor.close();
            }
        });
    }
}
