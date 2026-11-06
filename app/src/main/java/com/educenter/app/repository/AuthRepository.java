package com.educenter.app.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.educenter.app.EduCenterApplication;
import com.educenter.app.data.local.DatabaseHelper;
import com.educenter.app.data.local.SessionManager;
import com.educenter.app.model.User;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.DateUtils;
import com.educenter.app.utils.PasswordUtils;

import java.util.concurrent.ExecutorService;

/**
 * Repository handling Authentication, Registration, Session, and User Profile.
 */
public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final DatabaseHelper dbHelper;
    private final SessionManager sessionManager;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public AuthRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.sessionManager = SessionManager.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void login(String username, String password, boolean remember, DataCallback<User> callback) {
        executor.execute(() -> {
            Cursor cursor = null;
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = "SELECT u.id, u.username, u.password_hash, u.password_salt, "
                        + "u.full_name, u.email, u.phone, u.role_id, u.active, u.created_at, r.name as role_name "
                        + "FROM " + DatabaseHelper.TABLE_USERS + " u "
                        + "JOIN " + DatabaseHelper.TABLE_ROLES + " r ON u.role_id = r.id "
                        + "WHERE u.username = ?";

                cursor = db.rawQuery(query, new String[]{username.trim()});
                if (cursor.moveToFirst()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    String uname = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                    String hash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
                    String salt = cursor.getString(cursor.getColumnIndexOrThrow("password_salt"));
                    String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                    long roleId = cursor.getLong(cursor.getColumnIndexOrThrow("role_id"));
                    int active = cursor.getInt(cursor.getColumnIndexOrThrow("active"));
                    String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                    String roleName = cursor.getString(cursor.getColumnIndexOrThrow("role_name"));

                    // Check active status
                    if (active == 0) {
                        mainHandler.post(() -> callback.onError("Tài khoản của bạn đã bị khóa, vui lòng liên hệ Quản trị viên!"));
                        return;
                    }

                    // Verify password
                    boolean match = PasswordUtils.verifyPassword(password, hash, salt);
                    if (!match) {
                        mainHandler.post(() -> callback.onError("Mật khẩu không chính xác!"));
                        return;
                    }

                    // Success: construct user & save session
                    User user = new User(id, uname, hash, salt, fullName, email, phone, roleId, active, createdAt);
                    user.setRoleName(roleName);
                    sessionManager.createLoginSession(id, uname, fullName, roleName, remember);

                    mainHandler.post(() -> callback.onSuccess(user));
                } else {
                    mainHandler.post(() -> callback.onError("Tên đăng nhập không tồn tại!"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Login exception", e);
                mainHandler.post(() -> callback.onError("Lỗi khi đăng nhập: " + e.getMessage()));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }

    public void register(String fullName, String username, String email, String phone,
                         String password, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Check if username already exists
                Cursor checkCursor = db.rawQuery(
                        "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE username = ?",
                        new String[]{username.trim()}
                );
                try {
                    if (checkCursor.moveToFirst() && checkCursor.getInt(0) > 0) {
                        mainHandler.post(() -> callback.onError("Tên đăng nhập đã tồn tại trong hệ thống!"));
                        return;
                    }
                } finally {
                    checkCursor.close();
                }

                // Get STAFF role id
                long staffRoleId = 3;
                Cursor roleCursor = db.rawQuery(
                        "SELECT id FROM " + DatabaseHelper.TABLE_ROLES + " WHERE name = ?",
                        new String[]{Constants.ROLE_STAFF}
                );
                try {
                    if (roleCursor.moveToFirst()) {
                        staffRoleId = roleCursor.getLong(0);
                    }
                } finally {
                    roleCursor.close();
                }

                // Hash password
                String salt = PasswordUtils.generateSalt();
                String hash = PasswordUtils.hashPassword(password, salt);

                ContentValues cv = new ContentValues();
                cv.put("username", username.trim());
                cv.put("password_hash", hash);
                cv.put("password_salt", salt);
                cv.put("full_name", fullName.trim());
                cv.put("email", email != null ? email.trim() : "");
                cv.put("phone", phone != null ? phone.trim() : "");
                cv.put("role_id", staffRoleId);
                cv.put("active", 1);
                cv.put("created_at", DateUtils.getCurrentDateTimeDb());

                long result = db.insert(DatabaseHelper.TABLE_USERS, null, cv);
                if (result > 0) {
                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onError("Đăng ký tài khoản thất bại, vui lòng thử lại."));
                }
            } catch (Exception e) {
                Log.e(TAG, "Register exception", e);
                mainHandler.post(() -> callback.onError("Lỗi khi đăng ký: " + e.getMessage()));
            }
        });
    }

    public void getUserById(long userId, DataCallback<User> callback) {
        executor.execute(() -> {
            Cursor cursor = null;
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = "SELECT u.id, u.username, u.password_hash, u.password_salt, "
                        + "u.full_name, u.email, u.phone, u.role_id, u.active, u.created_at, r.name as role_name "
                        + "FROM " + DatabaseHelper.TABLE_USERS + " u "
                        + "JOIN " + DatabaseHelper.TABLE_ROLES + " r ON u.role_id = r.id "
                        + "WHERE u.id = ?";
                cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
                if (cursor.moveToFirst()) {
                    User user = new User(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("username")),
                            cursor.getString(cursor.getColumnIndexOrThrow("password_hash")),
                            cursor.getString(cursor.getColumnIndexOrThrow("password_salt")),
                            cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("email")),
                            cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("role_id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("active")),
                            cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                    );
                    user.setRoleName(cursor.getString(cursor.getColumnIndexOrThrow("role_name")));
                    mainHandler.post(() -> callback.onSuccess(user));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy thông tin tài khoản!"));
                }
            } catch (Exception e) {
                Log.e(TAG, "getUserById exception", e);
                mainHandler.post(() -> callback.onError("Lỗi truy vấn: " + e.getMessage()));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }

    public void updateProfile(long userId, String fullName, String email, String phone, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("full_name", fullName.trim());
                cv.put("email", email.trim());
                cv.put("phone", phone.trim());

                int rows = db.update(DatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(userId)});
                if (rows > 0) {
                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onError("Cập nhật thông tin thất bại."));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi cập nhật: " + e.getMessage()));
            }
        });
    }

    public void changePassword(long userId, String oldPassword, String newPassword, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            Cursor cursor = null;
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                cursor = db.rawQuery(
                        "SELECT password_hash, password_salt FROM " + DatabaseHelper.TABLE_USERS + " WHERE id = ?",
                        new String[]{String.valueOf(userId)}
                );
                if (cursor.moveToFirst()) {
                    String hash = cursor.getString(0);
                    String salt = cursor.getString(1);
                    if (!PasswordUtils.verifyPassword(oldPassword, hash, salt)) {
                        mainHandler.post(() -> callback.onError("Mật khẩu hiện tại không chính xác!"));
                        return;
                    }

                    String newSalt = PasswordUtils.generateSalt();
                    String newHash = PasswordUtils.hashPassword(newPassword, newSalt);

                    ContentValues cv = new ContentValues();
                    cv.put("password_hash", newHash);
                    cv.put("password_salt", newSalt);

                    int rows = db.update(DatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(userId)});
                    if (rows > 0) {
                        mainHandler.post(() -> callback.onSuccess(true));
                    } else {
                        mainHandler.post(() -> callback.onError("Đổi mật khẩu thất bại."));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Tài khoản không tồn tại!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi đổi mật khẩu: " + e.getMessage()));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }
}
