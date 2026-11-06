package com.educenter.app.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.educenter.app.EduCenterApplication;
import com.educenter.app.data.local.DatabaseHelper;
import com.educenter.app.model.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for Schedule management and Overlap Conflict Detection.
 */
public class ScheduleRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public ScheduleRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getAllSchedules(DataCallback<List<Schedule>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT sc.*, c.name as class_name, c.code as class_code, r.name as room_name, t.full_name as teacher_name "
                    + "FROM " + DatabaseHelper.TABLE_SCHEDULES + " sc "
                    + "JOIN " + DatabaseHelper.TABLE_CLASSES + " c ON sc.class_id = c.id "
                    + "JOIN " + DatabaseHelper.TABLE_ROOMS + " r ON sc.room_id = r.id "
                    + "JOIN " + DatabaseHelper.TABLE_TEACHERS + " t ON c.teacher_id = t.id "
                    + "ORDER BY sc.day_of_week ASC, sc.start_time ASC";

            Cursor cursor = db.rawQuery(sql, null);
            List<Schedule> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    Schedule s = new Schedule(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("class_id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("day_of_week")),
                            cursor.getString(cursor.getColumnIndexOrThrow("start_time")),
                            cursor.getString(cursor.getColumnIndexOrThrow("end_time")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("room_id"))
                    );
                    s.setClassName(cursor.getString(cursor.getColumnIndexOrThrow("class_name")));
                    s.setClassCode(cursor.getString(cursor.getColumnIndexOrThrow("class_code")));
                    s.setRoomName(cursor.getString(cursor.getColumnIndexOrThrow("room_name")));
                    s.setTeacherName(cursor.getString(cursor.getColumnIndexOrThrow("teacher_name")));
                    list.add(s);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi truy vấn thời khóa biểu: " + e.getMessage()));
            } finally {
                cursor.close();
            }
        });
    }

    public void getSchedulesByClass(long classId, DataCallback<List<Schedule>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT sc.*, c.name as class_name, c.code as class_code, r.name as room_name, t.full_name as teacher_name "
                    + "FROM " + DatabaseHelper.TABLE_SCHEDULES + " sc "
                    + "JOIN " + DatabaseHelper.TABLE_CLASSES + " c ON sc.class_id = c.id "
                    + "JOIN " + DatabaseHelper.TABLE_ROOMS + " r ON sc.room_id = r.id "
                    + "JOIN " + DatabaseHelper.TABLE_TEACHERS + " t ON c.teacher_id = t.id "
                    + "WHERE sc.class_id = ? "
                    + "ORDER BY sc.day_of_week ASC, sc.start_time ASC";

            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(classId)});
            List<Schedule> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    Schedule s = new Schedule(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("class_id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("day_of_week")),
                            cursor.getString(cursor.getColumnIndexOrThrow("start_time")),
                            cursor.getString(cursor.getColumnIndexOrThrow("end_time")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("room_id"))
                    );
                    s.setClassName(cursor.getString(cursor.getColumnIndexOrThrow("class_name")));
                    s.setClassCode(cursor.getString(cursor.getColumnIndexOrThrow("class_code")));
                    s.setRoomName(cursor.getString(cursor.getColumnIndexOrThrow("room_name")));
                    s.setTeacherName(cursor.getString(cursor.getColumnIndexOrThrow("teacher_name")));
                    list.add(s);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } finally {
                cursor.close();
            }
        });
    }

    public void insertSchedule(Schedule schedule, DataCallback<Long> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Get teacher of the class
            Cursor teacherCursor = db.rawQuery(
                    "SELECT teacher_id FROM " + DatabaseHelper.TABLE_CLASSES + " WHERE id = ?",
                    new String[]{String.valueOf(schedule.getClassId())}
            );
            long teacherId = -1;
            if (teacherCursor.moveToFirst()) {
                teacherId = teacherCursor.getLong(0);
            }
            teacherCursor.close();

            if (teacherId == -1) {
                mainHandler.post(() -> callback.onError("Lớp học không tồn tại!"));
                return;
            }

            // TC15: Check Teacher schedule conflict
            String teacherConflictSql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SCHEDULES + " sc "
                    + "JOIN " + DatabaseHelper.TABLE_CLASSES + " c ON sc.class_id = c.id "
                    + "WHERE c.teacher_id = ? AND sc.day_of_week = ? "
                    + "AND ((sc.start_time < ? AND sc.end_time > ?) OR (sc.start_time >= ? AND sc.start_time < ?))";

            Cursor tCheck = db.rawQuery(teacherConflictSql, new String[]{
                    String.valueOf(teacherId),
                    String.valueOf(schedule.getDayOfWeek()),
                    schedule.getEndTime(), schedule.getStartTime(),
                    schedule.getStartTime(), schedule.getEndTime()
            });
            try {
                if (tCheck.moveToFirst() && tCheck.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Trùng lịch giảng dạy của giáo viên vào thời gian này!"));
                    return;
                }
            } finally {
                tCheck.close();
            }

            // TC16: Check Room schedule conflict
            String roomConflictSql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SCHEDULES + " sc "
                    + "WHERE sc.room_id = ? AND sc.day_of_week = ? "
                    + "AND ((sc.start_time < ? AND sc.end_time > ?) OR (sc.start_time >= ? AND sc.start_time < ?))";

            Cursor rCheck = db.rawQuery(roomConflictSql, new String[]{
                    String.valueOf(schedule.getRoomId()),
                    String.valueOf(schedule.getDayOfWeek()),
                    schedule.getEndTime(), schedule.getStartTime(),
                    schedule.getStartTime(), schedule.getEndTime()
            });
            try {
                if (rCheck.moveToFirst() && rCheck.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Phòng học đã có lớp khác sử dụng vào thời gian này!"));
                    return;
                }
            } finally {
                rCheck.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("class_id", schedule.getClassId());
            cv.put("day_of_week", schedule.getDayOfWeek());
            cv.put("start_time", schedule.getStartTime());
            cv.put("end_time", schedule.getEndTime());
            cv.put("room_id", schedule.getRoomId());

            long id = db.insert(DatabaseHelper.TABLE_SCHEDULES, null, cv);
            if (id > 0) {
                mainHandler.post(() -> callback.onSuccess(id));
            } else {
                mainHandler.post(() -> callback.onError("Thêm lịch học thất bại."));
            }
        });
    }

    public void deleteSchedule(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete(DatabaseHelper.TABLE_SCHEDULES, "id = ?", new String[]{String.valueOf(id)});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Xóa lịch học thất bại."));
            }
        });
    }
}
