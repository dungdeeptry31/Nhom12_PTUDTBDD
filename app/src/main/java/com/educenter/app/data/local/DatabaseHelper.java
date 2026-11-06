package com.educenter.app.data.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.educenter.app.utils.Constants;

/**
 * Main SQLiteOpenHelper managing SQLite database lifecycle, schema DDL,
 * table constraints, and self-healing schema creation for EduCenter.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;
    private final Context context;

    // --- TABLE NAMES ---
    public static final String TABLE_ROLES = "roles";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SUBJECTS = "subjects";
    public static final String TABLE_TEACHERS = "teachers";
    public static final String TABLE_ROOMS = "rooms";
    public static final String TABLE_CLASSES = "classes";
    public static final String TABLE_SCHEDULES = "schedules";
    public static final String TABLE_STUDENTS = "students";
    public static final String TABLE_CLASS_STUDENTS = "class_students";
    public static final String TABLE_ATTENDANCE = "attendance";
    public static final String TABLE_TUITION_INVOICES = "tuition_invoices";
    public static final String TABLE_TUITION_PAYMENTS = "tuition_payments";
    public static final String TABLE_EXAM_RESULTS = "exam_results";

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        try {
            db.setForeignKeyConstraintsEnabled(true);
            Log.d(TAG, "SQLite foreign key constraints enabled via onConfigure.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable foreign keys: " + e.getMessage());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating EduCenter database schema (13 tables)...");
        createAllTables(db);
        DatabaseSeeder.seed(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Self-healing check: Verify if tables exist. If missing, initialize immediately.
        try {
            boolean fullyInitialized = false;
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='" + TABLE_TUITION_INVOICES + "'",
                    null
            );
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    fullyInitialized = cursor.getInt(0) > 0;
                }
                cursor.close();
            }

            if (!fullyInitialized) {
                Log.w(TAG, "Schema incomplete onOpen! Re-creating all tables and seeding...");
                createAllTables(db);
                DatabaseSeeder.seed(db);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking schema in onOpen: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        dropAllTables(db);
        createAllTables(db);
        DatabaseSeeder.seed(db);
    }

    public void dropAllTables(SQLiteDatabase db) {
        String[] tables = {
                TABLE_EXAM_RESULTS, TABLE_TUITION_PAYMENTS, TABLE_TUITION_INVOICES,
                TABLE_ATTENDANCE, TABLE_CLASS_STUDENTS, TABLE_STUDENTS,
                TABLE_SCHEDULES, TABLE_CLASSES, TABLE_ROOMS, TABLE_TEACHERS,
                TABLE_SUBJECTS, TABLE_USERS, TABLE_ROLES
        };
        for (String table : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
    }

    private void createAllTables(SQLiteDatabase db) {
        // 1. Roles table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ROLES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE, "
                + "description TEXT"
                + ");");

        // 2. Users table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL, "
                + "password_salt TEXT NOT NULL, "
                + "full_name TEXT NOT NULL, "
                + "email TEXT, "
                + "phone TEXT, "
                + "role_id INTEGER NOT NULL, "
                + "active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0, 1)), "
                + "created_at TEXT NOT NULL, "
                + "FOREIGN KEY (role_id) REFERENCES " + TABLE_ROLES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT"
                + ");");

        // 3. Subjects table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SUBJECTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "code TEXT NOT NULL UNIQUE, "
                + "name TEXT NOT NULL UNIQUE, "
                + "description TEXT, "
                + "active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0, 1))"
                + ");");

        // 4. Teachers table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TEACHERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "full_name TEXT NOT NULL, "
                + "phone TEXT NOT NULL UNIQUE, "
                + "email TEXT NOT NULL UNIQUE, "
                + "specialization TEXT, "
                + "degree TEXT, "
                + "experience INTEGER DEFAULT 0, "
                + "active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0, 1))"
                + ");");

        // 5. Rooms table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ROOMS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE, "
                + "capacity INTEGER NOT NULL CHECK(capacity > 0), "
                + "location TEXT, "
                + "active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0, 1))"
                + ");");

        // 6. Classes table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CLASSES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "code TEXT NOT NULL UNIQUE, "
                + "name TEXT NOT NULL, "
                + "subject_id INTEGER NOT NULL, "
                + "teacher_id INTEGER NOT NULL, "
                + "room_id INTEGER NOT NULL, "
                + "max_students INTEGER NOT NULL CHECK(max_students > 0), "
                + "tuition_fee REAL NOT NULL CHECK(tuition_fee >= 0), "
                + "start_date TEXT NOT NULL, "
                + "end_date TEXT NOT NULL, "
                + "status TEXT NOT NULL CHECK(status IN ('OPEN', 'UPCOMING', 'FINISHED')), "
                + "FOREIGN KEY (subject_id) REFERENCES " + TABLE_SUBJECTS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT, "
                + "FOREIGN KEY (teacher_id) REFERENCES " + TABLE_TEACHERS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT, "
                + "FOREIGN KEY (room_id) REFERENCES " + TABLE_ROOMS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT"
                + ");");

        // 7. Schedules table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SCHEDULES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "class_id INTEGER NOT NULL, "
                + "day_of_week INTEGER NOT NULL CHECK(day_of_week BETWEEN 1 AND 7), "
                + "start_time TEXT NOT NULL, "
                + "end_time TEXT NOT NULL, "
                + "room_id INTEGER NOT NULL, "
                + "FOREIGN KEY (class_id) REFERENCES " + TABLE_CLASSES + "(id) ON UPDATE CASCADE ON DELETE CASCADE, "
                + "FOREIGN KEY (room_id) REFERENCES " + TABLE_ROOMS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT"
                + ");");

        // 8. Students table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_STUDENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "student_code TEXT NOT NULL UNIQUE, "
                + "full_name TEXT NOT NULL, "
                + "date_of_birth TEXT NOT NULL, "
                + "gender TEXT NOT NULL, "
                + "phone TEXT, "
                + "parent_name TEXT, "
                + "parent_phone TEXT, "
                + "school TEXT, "
                + "grade_level TEXT, "
                + "address TEXT, "
                + "active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0, 1)), "
                + "created_at TEXT NOT NULL"
                + ");");

        // 9. Class_Students enrollment table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CLASS_STUDENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "class_id INTEGER NOT NULL, "
                + "student_id INTEGER NOT NULL, "
                + "enrolled_date TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE', "
                + "UNIQUE(class_id, student_id), "
                + "FOREIGN KEY (class_id) REFERENCES " + TABLE_CLASSES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT, "
                + "FOREIGN KEY (student_id) REFERENCES " + TABLE_STUDENTS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT"
                + ");");

        // 10. Attendance table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ATTENDANCE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "class_id INTEGER NOT NULL, "
                + "student_id INTEGER NOT NULL, "
                + "attendance_date TEXT NOT NULL, "
                + "status TEXT NOT NULL CHECK(status IN ('PRESENT', 'ABSENT', 'LATE')), "
                + "note TEXT, "
                + "UNIQUE(class_id, student_id, attendance_date), "
                + "FOREIGN KEY (class_id) REFERENCES " + TABLE_CLASSES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT, "
                + "FOREIGN KEY (student_id) REFERENCES " + TABLE_STUDENTS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT"
                + ");");

        // 11. Tuition Invoices table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TUITION_INVOICES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "invoice_code TEXT NOT NULL UNIQUE, "
                + "student_id INTEGER NOT NULL, "
                + "class_id INTEGER NOT NULL, "
                + "amount REAL NOT NULL CHECK(amount > 0), "
                + "due_date TEXT NOT NULL, "
                + "month_year TEXT NOT NULL, "
                + "note TEXT, "
                + "created_at TEXT NOT NULL, "
                + "FOREIGN KEY (student_id) REFERENCES " + TABLE_STUDENTS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT, "
                + "FOREIGN KEY (class_id) REFERENCES " + TABLE_CLASSES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT"
                + ");");

        // 12. Tuition Payments table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TUITION_PAYMENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "invoice_id INTEGER NOT NULL, "
                + "amount REAL NOT NULL CHECK(amount > 0), "
                + "payment_date TEXT NOT NULL, "
                + "payment_method TEXT NOT NULL CHECK(payment_method IN ('CASH', 'TRANSFER')), "
                + "note TEXT, "
                + "received_by INTEGER NOT NULL, "
                + "FOREIGN KEY (invoice_id) REFERENCES " + TABLE_TUITION_INVOICES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT, "
                + "FOREIGN KEY (received_by) REFERENCES " + TABLE_USERS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT"
                + ");");

        // 13. Exam Results table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EXAM_RESULTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "student_id INTEGER NOT NULL, "
                + "class_id INTEGER NOT NULL, "
                + "exam_name TEXT NOT NULL, "
                + "exam_date TEXT NOT NULL, "
                + "score REAL NOT NULL CHECK(score >= 0), "
                + "max_score REAL NOT NULL CHECK(max_score > 0), "
                + "note TEXT, "
                + "FOREIGN KEY (student_id) REFERENCES " + TABLE_STUDENTS + "(id) ON UPDATE CASCADE ON DELETE RESTRICT, "
                + "FOREIGN KEY (class_id) REFERENCES " + TABLE_CLASSES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT"
                + ");");

        Log.d(TAG, "All 13 tables verified/created successfully.");
    }
}
