package com.educenter.app.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.educenter.app.EduCenterApplication;
import com.educenter.app.data.local.DatabaseHelper;
import com.educenter.app.model.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for Subject management.
 */
public class SubjectRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public SubjectRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getAllSubjects(DataCallback<List<Subject>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT s.*, "
                    + "(SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASSES + " c WHERE c.subject_id = s.id AND c.status = 'OPEN') as active_classes_count "
                    + "FROM " + DatabaseHelper.TABLE_SUBJECTS + " s "
                    + "ORDER BY s.id DESC";

            Cursor cursor = db.rawQuery(sql, null);
            List<Subject> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    Subject s = new Subject(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("code")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("active"))
                    );
                    s.setActiveClassesCount(cursor.getInt(cursor.getColumnIndexOrThrow("active_classes_count")));
                    list.add(s);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi truy vấn môn học: " + e.getMessage()));
            } finally {
                cursor.close();
            }
        });
    }

    public void getSubjectById(long id, DataCallback<Subject> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT s.*, "
                    + "(SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASSES + " c WHERE c.subject_id = s.id AND c.status = 'OPEN') as active_classes_count "
                    + "FROM " + DatabaseHelper.TABLE_SUBJECTS + " s WHERE s.id = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            try {
                if (cursor.moveToFirst()) {
                    Subject s = new Subject(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("code")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("active"))
                    );
                    s.setActiveClassesCount(cursor.getInt(cursor.getColumnIndexOrThrow("active_classes_count")));
                    mainHandler.post(() -> callback.onSuccess(s));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy môn học."));
                }
            } finally {
                cursor.close();
            }
        });
    }

    public void insertSubject(Subject subject, DataCallback<Long> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check duplicate code
            Cursor checkCode = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SUBJECTS + " WHERE code = ?",
                    new String[]{subject.getCode().trim()}
            );
            try {
                if (checkCode.moveToFirst() && checkCode.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Mã môn học đã tồn tại!"));
                    return;
                }
            } finally {
                checkCode.close();
            }

            // TC08: Check duplicate name
            Cursor checkName = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SUBJECTS + " WHERE name = ?",
                    new String[]{subject.getName().trim()}
            );
            try {
                if (checkName.moveToFirst() && checkName.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Tên môn học đã tồn tại trong hệ thống!"));
                    return;
                }
            } finally {
                checkName.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("code", subject.getCode().trim().toUpperCase());
            cv.put("name", subject.getName().trim());
            cv.put("description", subject.getDescription());
            cv.put("active", subject.getActive());

            long id = db.insert(DatabaseHelper.TABLE_SUBJECTS, null, cv);
            if (id > 0) {
                mainHandler.post(() -> callback.onSuccess(id));
            } else {
                mainHandler.post(() -> callback.onError("Thêm môn học thất bại."));
            }
        });
    }

    public void updateSubject(Subject subject, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check duplicate name for other subjects
            Cursor checkName = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SUBJECTS + " WHERE name = ? AND id != ?",
                    new String[]{subject.getName().trim(), String.valueOf(subject.getId())}
            );
            try {
                if (checkName.moveToFirst() && checkName.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Tên môn học đã bị trùng với môn học khác!"));
                    return;
                }
            } finally {
                checkName.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("code", subject.getCode().trim().toUpperCase());
            cv.put("name", subject.getName().trim());
            cv.put("description", subject.getDescription());
            cv.put("active", subject.getActive());

            int rows = db.update(DatabaseHelper.TABLE_SUBJECTS, cv, "id = ?", new String[]{String.valueOf(subject.getId())});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Cập nhật môn học thất bại."));
            }
        });
    }

    public void deleteSubject(long id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // TC10: Check if subject is used by any class
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASSES + " WHERE subject_id = ?",
                    new String[]{String.valueOf(id)}
            );
            try {
                if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Không thể xóa môn học này vì đang được sử dụng trong các lớp học."));
                    return;
                }
            } finally {
                cursor.close();
            }

            // TC11: Unused subject can be deleted cleanly
            int rows = db.delete(DatabaseHelper.TABLE_SUBJECTS, "id = ?", new String[]{String.valueOf(id)});
            if (rows > 0) {
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onError("Xóa môn học thất bại."));
            }
        });
    }
}
