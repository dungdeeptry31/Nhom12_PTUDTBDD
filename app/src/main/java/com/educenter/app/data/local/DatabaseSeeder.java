package com.educenter.app.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.educenter.app.utils.Constants;
import com.educenter.app.utils.PasswordUtils;

/**
 * Seeds comprehensive, realistic Vietnamese sample data on initial database creation.
 * Idempotent: checks for existing admin user and executes safely inside or outside transactions.
 */
public final class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";

    private DatabaseSeeder() {
        // Utility class
    }

    public static void seed(SQLiteDatabase db) {
        // Idempotency check: if admin and tuition invoices already exist, skip seeding
        try {
            Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'", null);
            Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM tuition_invoices", null);
            boolean hasAdmin = false;
            boolean hasInvoices = false;
            if (c1 != null) {
                if (c1.moveToFirst()) hasAdmin = c1.getInt(0) > 0;
                c1.close();
            }
            if (c2 != null) {
                if (c2.moveToFirst()) hasInvoices = c2.getInt(0) > 0;
                c2.close();
            }
            if (hasAdmin && hasInvoices) {
                Log.d(TAG, "Database already seeded. Skipping.");
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, "Checking users or invoices table failed (schema might be fresh): " + e.getMessage());
        }

        Log.d(TAG, "Starting database seeding with realistic Vietnamese data...");

        boolean isInsideTransaction = db.inTransaction();
        if (!isInsideTransaction) {
            db.beginTransaction();
        }
        try {
            // 1. SEED ROLES
            long adminRoleId = insertRole(db, Constants.ROLE_ADMIN, "Quản trị viên toàn quyền hệ thống");
            long teacherRoleId = insertRole(db, Constants.ROLE_TEACHER, "Giáo viên giảng dạy và quản lý lớp");
            long staffRoleId = insertRole(db, Constants.ROLE_STAFF, "Nhân viên hỗ trợ học vụ và thu ngân");

            // 2. SEED USERS (Password demo: 123456)
            String saltAdmin = PasswordUtils.generateSalt();
            String hashAdmin = PasswordUtils.hashPassword("123456", saltAdmin);
            long adminUserId = insertUser(db, "admin", hashAdmin, saltAdmin, "Trần Quản Trị",
                    "admin@educenter.edu.vn", "0901112233", adminRoleId, 1, "2026-01-01 08:00:00");

            String saltTeacher = PasswordUtils.generateSalt();
            String hashTeacher = PasswordUtils.hashPassword("123456", saltTeacher);
            insertUser(db, "giaovien", hashTeacher, saltTeacher, "Nguyễn Văn An",
                    "nguyenvanan@educenter.edu.vn", "0912345601", teacherRoleId, 1, "2026-01-01 08:00:00");

            String saltLocked = PasswordUtils.generateSalt();
            String hashLocked = PasswordUtils.hashPassword("123456", saltLocked);
            insertUser(db, "bikhoa", hashLocked, saltLocked, "Lê Nhân Viên",
                    "lenhanvien@educenter.edu.vn", "0988776655", staffRoleId, 0, "2026-01-01 08:00:00");

            // 3. SEED SUBJECTS (6 subjects, TINTH is unused for TC11 testing)
            long subToan = insertSubject(db, "TOAN12", "Toán 12", "Toán học ôn thi THPT Quốc gia", 1);
            long subLy = insertSubject(db, "LY12", "Vật lý 12", "Vật lý luyện thi Đại học chuyên sâu", 1);
            long subHoa = insertSubject(db, "HOA11", "Hóa học 11", "Hóa học cơ bản và nâng cao lớp 11", 1);
            long subIelts = insertSubject(db, "IELTS", "Tiếng Anh IELTS", "Luyện thi chứng chỉ IELTS 6.5+", 1);
            long subVan = insertSubject(db, "VAN9", "Ngữ văn 9", "Ôn thi vào lớp 10 môn Ngữ văn", 1);
            insertSubject(db, "TINTH", "Tin học trẻ", "Lập trình cơ bản cho học sinh (Chưa mở lớp)", 1);

            // 4. SEED TEACHERS (6 teachers, Teacher 6 is unassigned for TC09 testing)
            long gvAn = insertTeacher(db, "Nguyễn Văn An", "0912345601", "nguyenvanan@educenter.edu.vn",
                    "Toán học", "Thạc sĩ Toán học", 8, 1);
            long gvMai = insertTeacher(db, "Trần Thị Mai", "0912345602", "tranthimai@educenter.edu.vn",
                    "Vật lý", "Cử nhân Sư phạm Lý", 5, 1);
            long gvNam = insertTeacher(db, "Lê Hoàng Nam", "0912345603", "lehoangnam@educenter.edu.vn",
                    "Hóa học", "Thạc sĩ Hóa học", 6, 1);
            long gvDuc = insertTeacher(db, "Phạm Minh Đức", "0912345604", "phamminhduc@educenter.edu.vn",
                    "Tiếng Anh", "Cử nhân Ngôn ngữ Anh (IELTS 8.0)", 4, 1);
            long gvHa = insertTeacher(db, "Vũ Thu Hà", "0912345605", "vuthuha@educenter.edu.vn",
                    "Ngữ văn", "Thạc sĩ Ngữ văn", 7, 1);
            insertTeacher(db, "Đỗ Quốc Tuấn", "0912345606", "doquoctuan@educenter.edu.vn",
                    "Tin học", "Kỹ sư CNTT", 3, 1);

            // 5. SEED ROOMS (5 rooms)
            long room101 = insertRoom(db, "P.101", 25, "Tầng 1, Nhà A", 1);
            long room102 = insertRoom(db, "P.102", 30, "Tầng 1, Nhà A", 1);
            long room103 = insertRoom(db, "P.103", 20, "Tầng 1, Nhà B", 1);
            long room201 = insertRoom(db, "P.201", 15, "Tầng 2, Nhà B", 1);
            long room202 = insertRoom(db, "P.202", 35, "Tầng 2, Nhà A", 1);

            // 6. SEED CLASSES (6 classes)
            // Class 1: TOAN12A - OPEN
            long cToan12A = insertClass(db, "TOAN12A", "Toán 12 Nâng cao", subToan, gvAn, room101,
                    25, 1500000.0, "2026-06-01", "2026-12-31", Constants.STATUS_CLASS_OPEN);

            // Class 2: LY12A - OPEN
            long cLy12A = insertClass(db, "LY12A", "Luyện thi ĐH Vật lý 12", subLy, gvMai, room102,
                    20, 1400000.0, "2026-06-01", "2026-12-31", Constants.STATUS_CLASS_OPEN);

            // Class 3: HOA11A - OPEN, MAX = 5 (Đặc biệt xếp đủ 5 để test TC13!)
            long cHoa11A = insertClass(db, "HOA11A", "Hóa học 11 Chuyên đề", subHoa, gvNam, room103,
                    5, 1200000.0, "2026-06-01", "2026-12-31", Constants.STATUS_CLASS_OPEN);

            // Class 4: IELTS65 - UPCOMING
            long cIelts = insertClass(db, "IELTS65", "Luyện thi IELTS 6.5+", subIelts, gvDuc, room201,
                    15, 2500000.0, "2026-11-15", "2027-03-31", Constants.STATUS_CLASS_UPCOMING);

            // Class 5: VAN9A - FINISHED
            long cVan9A = insertClass(db, "VAN9A", "Ôn thi vào 10 Ngữ văn", subVan, gvHa, room202,
                    30, 1100000.0, "2026-01-05", "2026-06-30", Constants.STATUS_CLASS_FINISHED);

            // Class 6: TOAN12B - OPEN
            long cToan12B = insertClass(db, "TOAN12B", "Chuyên đề Hình học 12", subToan, gvAn, room101,
                    20, 1300000.0, "2026-09-01", "2026-12-31", Constants.STATUS_CLASS_OPEN);

            // 7. SEED SCHEDULES
            insertSchedule(db, cToan12A, 2, "18:00", "19:30", room101);
            insertSchedule(db, cToan12A, 4, "18:00", "19:30", room101);
            insertSchedule(db, cToan12A, 6, "18:00", "19:30", room101);

            insertSchedule(db, cLy12A, 3, "18:00", "19:30", room102);
            insertSchedule(db, cLy12A, 5, "18:00", "19:30", room102);

            insertSchedule(db, cHoa11A, 7, "08:30", "10:00", room103);
            insertSchedule(db, cHoa11A, 1, "08:30", "10:00", room103);

            insertSchedule(db, cIelts, 3, "19:45", "21:15", room201);
            insertSchedule(db, cIelts, 5, "19:45", "21:15", room201);

            insertSchedule(db, cToan12B, 7, "14:30", "16:00", room101);
            insertSchedule(db, cToan12B, 1, "14:30", "16:00", room101);

            // 8. SEED STUDENTS (14 students)
            long s1 = insertStudent(db, "HS001", "Hoàng Gia Bảo", "2009-03-15", "Nam", "0971001001",
                    "Hoàng Văn Long", "0981001001", "THPT Cầu Giấy", "11", "Số 12 Cầu Giấy, Hà Nội", 1, "2026-05-10");
            long s2 = insertStudent(db, "HS002", "Nguyễn Ngọc Ánh", "2009-08-20", "Nữ", "0971001002",
                    "Nguyễn Văn Bình", "0981001002", "THPT Yên Hòa", "11", "Số 45 Trần Thái Tông, Hà Nội", 1, "2026-05-12");
            long s3 = insertStudent(db, "HS003", "Trần Minh Quang", "2009-01-10", "Nam", "0971001003",
                    "Trần Quốc Huy", "0981001003", "THPT Chuyên Sư Phạm", "11", "Số 88 Xuân Thủy, Hà Nội", 1, "2026-05-15");
            long s4 = insertStudent(db, "HS004", "Phạm Thùy Linh", "2009-11-25", "Nữ", "0971001004",
                    "Phạm Thanh Tùng", "0981001004", "THPT Kim Liên", "11", "Số 20 Đào Tấn, Hà Nội", 1, "2026-05-18");
            long s5 = insertStudent(db, "HS005", "Đỗ Đức Huy", "2009-05-04", "Nam", "0971001005",
                    "Đỗ Minh Tâm", "0981001005", "THPT Chuyên Ngoại Ngữ", "11", "Số 15 Hoàng Quốc Việt, Hà Nội", 1, "2026-05-20");

            long s6 = insertStudent(db, "HS006", "Vũ Mai Phương", "2008-07-12", "Nữ", "0971001006",
                    "Vũ Đình Trọng", "0981001006", "THPT Thăng Long", "12", "Số 99 Lê Duẩn, Hà Nội", 1, "2026-05-22");
            long s7 = insertStudent(db, "HS007", "Lê Tuấn Kiệt", "2008-09-30", "Nam", "0971001007",
                    "Lê Văn Tuấn", "0981001007", "THPT Việt Đức", "12", "Số 32 Lý Thường Kiệt, Hà Nội", 1, "2026-05-25");
            long s8 = insertStudent(db, "HS008", "Bùi Khánh Linh", "2008-12-05", "Nữ", "0971001008",
                    "Bùi Quang Hải", "0981001008", "THPT Chu Văn An", "12", "Số 10 Thụy Khuê, Hà Nội", 1, "2026-05-28");
            long s9 = insertStudent(db, "HS009", "Đặng Quốc Anh", "2008-04-18", "Nam", "0971001009",
                    "Đặng Văn Minh", "0981001009", "THPT Cầu Giấy", "12", "Số 71 Nguyễn Khang, Hà Nội", 1, "2026-06-01");
            long s10 = insertStudent(db, "HS010", "Ngô Quỳnh Trang", "2008-06-22", "Nữ", "0971001010",
                    "Ngô Đình Trung", "0981001010", "THPT Yên Hòa", "12", "Số 56 Trung Kính, Hà Nội", 1, "2026-06-02");

            long s11 = insertStudent(db, "HS011", "Phan Nhật Nam", "2008-10-14", "Nam", "0971001011",
                    "Phan Văn Thắng", "0981001011", "THPT Chuyên Hà Nội - Amsterdam", "12", "Số 1 Hoàng Minh Giám, Hà Nội", 1, "2026-06-03");
            long s12 = insertStudent(db, "HS012", "Trịnh Phương Thảo", "2008-02-28", "Nữ", "0971001012",
                    "Trịnh Xuân Bách", "0981001012", "THPT Kim Liên", "12", "Số 80 Phạm Ngọc Thạch, Hà Nội", 1, "2026-06-04");
            long s13 = insertStudent(db, "HS013", "Dương Tiến Đạt", "2011-04-09", "Nam", "0971001013",
                    "Dương Văn Hùng", "0981001013", "THCS Nghĩa Tân", "9", "Số 23 Tô Hiệu, Hà Nội", 1, "2026-01-02");
            long s14 = insertStudent(db, "HS014", "Mai Thảo Vy", "2011-09-17", "Nữ", "0971001014",
                    "Mai Văn Hưng", "0981001014", "THCS Cầu Giấy", "9", "Số 5 Nguyễn Phong Sắc, Hà Nội", 1, "2026-01-03");

            // 9. SEED ENROLLMENTS (class_students)
            insertClassStudent(db, cHoa11A, s1, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cHoa11A, s2, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cHoa11A, s3, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cHoa11A, s4, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cHoa11A, s5, "2026-06-01", "ACTIVE");

            insertClassStudent(db, cToan12A, s6, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cToan12A, s7, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cToan12A, s8, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cToan12A, s9, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cToan12A, s10, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cToan12A, s11, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cToan12A, s12, "2026-06-01", "ACTIVE");

            insertClassStudent(db, cLy12A, s6, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cLy12A, s7, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cLy12A, s8, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cLy12A, s9, "2026-06-01", "ACTIVE");
            insertClassStudent(db, cLy12A, s11, "2026-06-01", "ACTIVE");

            insertClassStudent(db, cIelts, s8, "2026-11-01", "ACTIVE");
            insertClassStudent(db, cIelts, s10, "2026-11-01", "ACTIVE");
            insertClassStudent(db, cIelts, s12, "2026-11-01", "ACTIVE");

            insertClassStudent(db, cToan12B, s6, "2026-09-01", "ACTIVE");
            insertClassStudent(db, cToan12B, s8, "2026-09-01", "ACTIVE");
            insertClassStudent(db, cToan12B, s10, "2026-09-01", "ACTIVE");
            insertClassStudent(db, cToan12B, s12, "2026-09-01", "ACTIVE");

            // 10. SEED ATTENDANCE
            insertAttendance(db, cToan12A, s6, "2026-10-19", Constants.ATTENDANCE_PRESENT, "Đi học đúng giờ");
            insertAttendance(db, cToan12A, s7, "2026-10-19", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cToan12A, s8, "2026-10-19", Constants.ATTENDANCE_LATE, "Đi muộn 10 phút");
            insertAttendance(db, cToan12A, s9, "2026-10-19", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cToan12A, s10, "2026-10-19", Constants.ATTENDANCE_ABSENT, "Có phép phụ huynh");
            insertAttendance(db, cToan12A, s11, "2026-10-19", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cToan12A, s12, "2026-10-19", Constants.ATTENDANCE_PRESENT, "");

            insertAttendance(db, cToan12A, s6, "2026-10-21", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cToan12A, s7, "2026-10-21", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cToan12A, s8, "2026-10-21", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cToan12A, s9, "2026-10-21", Constants.ATTENDANCE_LATE, "Tắc đường");
            insertAttendance(db, cToan12A, s10, "2026-10-21", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cToan12A, s11, "2026-10-21", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cToan12A, s12, "2026-10-21", Constants.ATTENDANCE_ABSENT, "Ốm sốt");

            insertAttendance(db, cHoa11A, s1, "2026-10-24", Constants.ATTENDANCE_PRESENT, "Hăng hái phát biểu");
            insertAttendance(db, cHoa11A, s2, "2026-10-24", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cHoa11A, s3, "2026-10-24", Constants.ATTENDANCE_PRESENT, "");
            insertAttendance(db, cHoa11A, s4, "2026-10-24", Constants.ATTENDANCE_LATE, "Muộn 5 phút");
            insertAttendance(db, cHoa11A, s5, "2026-10-24", Constants.ATTENDANCE_ABSENT, "Trùng lịch thi trường");

            // 11. SEED TUITION INVOICES & PAYMENTS
            seedInvoiceWithPayments(db, "HD202606-001", s6, cToan12A, 1500000.0, "2026-06-10", "06/2026",
                    "Học phí tháng 6", "2026-06-01", adminUserId, 1500000.0, "2026-06-05", Constants.PAYMENT_METHOD_TRANSFER);
            seedInvoiceWithPayments(db, "HD202606-002", s7, cToan12A, 1500000.0, "2026-06-10", "06/2026",
                    "Học phí tháng 6", "2026-06-01", adminUserId, 1500000.0, "2026-06-06", Constants.PAYMENT_METHOD_CASH);
            seedInvoiceWithPayments(db, "HD202606-003", s1, cHoa11A, 1200000.0, "2026-06-10", "06/2026",
                    "Học phí tháng 6", "2026-06-01", adminUserId, 1200000.0, "2026-06-07", Constants.PAYMENT_METHOD_TRANSFER);

            seedInvoiceWithPayments(db, "HD202607-001", s6, cToan12A, 1500000.0, "2026-07-10", "07/2026",
                    "Học phí tháng 7", "2026-07-01", adminUserId, 1500000.0, "2026-07-05", Constants.PAYMENT_METHOD_TRANSFER);
            seedInvoiceWithPayments(db, "HD202607-002", s7, cToan12A, 1500000.0, "2026-07-10", "07/2026",
                    "Học phí tháng 7", "2026-07-01", adminUserId, 1500000.0, "2026-07-08", Constants.PAYMENT_METHOD_CASH);
            seedInvoiceWithPayments(db, "HD202607-003", s2, cHoa11A, 1200000.0, "2026-07-10", "07/2026",
                    "Học phí tháng 7", "2026-07-01", adminUserId, 1200000.0, "2026-07-09", Constants.PAYMENT_METHOD_TRANSFER);

            seedInvoiceWithPayments(db, "HD202608-001", s6, cToan12A, 1500000.0, "2026-08-10", "08/2026",
                    "Học phí tháng 8", "2026-08-01", adminUserId, 1500000.0, "2026-08-04", Constants.PAYMENT_METHOD_TRANSFER);
            seedInvoiceWithPayments(db, "HD202608-002", s8, cLy12A, 1400000.0, "2026-08-10", "08/2026",
                    "Học phí tháng 8", "2026-08-01", adminUserId, 1400000.0, "2026-08-05", Constants.PAYMENT_METHOD_CASH);
            seedInvoiceWithPayments(db, "HD202608-003", s3, cHoa11A, 1200000.0, "2026-08-10", "08/2026",
                    "Học phí tháng 8", "2026-08-01", adminUserId, 1200000.0, "2026-08-08", Constants.PAYMENT_METHOD_TRANSFER);

            seedInvoiceWithPayments(db, "HD202609-001", s6, cToan12A, 1500000.0, "2026-09-10", "09/2026",
                    "Học phí tháng 9", "2026-09-01", adminUserId, 1500000.0, "2026-09-05", Constants.PAYMENT_METHOD_TRANSFER);
            seedInvoiceWithPayments(db, "HD202609-002", s9, cLy12A, 1400000.0, "2026-09-10", "09/2026",
                    "Học phí tháng 9", "2026-09-01", adminUserId, 1400000.0, "2026-09-07", Constants.PAYMENT_METHOD_TRANSFER);
            seedInvoiceWithPayments(db, "HD202609-003", s4, cHoa11A, 1200000.0, "2026-09-10", "09/2026",
                    "Học phí tháng 9", "2026-09-01", adminUserId, 1200000.0, "2026-09-09", Constants.PAYMENT_METHOD_CASH);

            seedInvoiceWithPayments(db, "HD202610-001", s6, cToan12A, 1500000.0, "2026-10-10", "10/2026",
                    "Học phí tháng 10", "2026-10-01", adminUserId, 1500000.0, "2026-10-05", Constants.PAYMENT_METHOD_TRANSFER);
            seedInvoiceWithPayments(db, "HD202610-002", s7, cToan12A, 1500000.0, "2026-10-10", "10/2026",
                    "Học phí tháng 10", "2026-10-01", adminUserId, 1500000.0, "2026-10-06", Constants.PAYMENT_METHOD_CASH);
            seedInvoiceWithPayments(db, "HD202610-003", s8, cLy12A, 1400000.0, "2026-10-10", "10/2026",
                    "Học phí tháng 10 đóng đợt 1", "2026-10-01", adminUserId, 800000.0, "2026-10-08", Constants.PAYMENT_METHOD_TRANSFER);

            seedInvoiceWithPayments(db, "HD202611-001", s6, cToan12A, 1500000.0, "2026-11-10", "11/2026",
                    "Học phí tháng 11", "2026-11-01", adminUserId, 1500000.0, "2026-11-01", Constants.PAYMENT_METHOD_TRANSFER);
            seedInvoiceWithPayments(db, "HD202611-002", s7, cToan12A, 1500000.0, "2026-11-10", "11/2026",
                    "Đóng đợt 1", "2026-11-01", adminUserId, 500000.0, "2026-11-01", Constants.PAYMENT_METHOD_CASH);
            seedInvoiceWithPayments(db, "HD202611-003", s8, cToan12A, 1500000.0, "2026-11-10", "11/2026",
                    "Chưa đóng học phí tháng 11", "2026-11-01", adminUserId, 0.0, null, null);
            seedInvoiceWithPayments(db, "HD202611-004", s1, cHoa11A, 1200000.0, "2026-11-10", "11/2026",
                    "Chưa đóng", "2026-11-01", adminUserId, 0.0, null, null);

            // 12. SEED EXAM RESULTS
            insertExamResult(db, s6, cToan12A, "Kiểm tra 15 phút - Đạo hàm", "2026-09-15", 9.0, 10.0, "Nắm chắc lý thuyết");
            insertExamResult(db, s7, cToan12A, "Kiểm tra 15 phút - Đạo hàm", "2026-09-15", 8.5, 10.0, "Tốt");
            insertExamResult(db, s8, cToan12A, "Kiểm tra 15 phút - Đạo hàm", "2026-09-15", 7.0, 10.0, "Cần cẩn thận khi tính");

            insertExamResult(db, s6, cToan12A, "Kiểm tra 1 tiết - Khảo sát hàm số", "2026-10-15", 9.5, 10.0, "Xuất sắc");
            insertExamResult(db, s7, cToan12A, "Kiểm tra 1 tiết - Khảo sát hàm số", "2026-10-15", 8.0, 10.0, "Khá");
            insertExamResult(db, s8, cToan12A, "Kiểm tra 1 tiết - Khảo sát hàm số", "2026-10-15", 6.5, 10.0, "Cần ôn thêm tiệm cận");

            insertExamResult(db, s1, cHoa11A, "Kiểm tra 15 phút - Sự điện li", "2026-09-20", 9.0, 10.0, "Hiểu bài");
            insertExamResult(db, s2, cHoa11A, "Kiểm tra 15 phút - Sự điện li", "2026-09-20", 8.5, 10.0, "Tốt");

            if (!isInsideTransaction) {
                db.setTransactionSuccessful();
            }
            Log.d(TAG, "Database seeded successfully with all tables and relationships!");
        } catch (Exception e) {
            Log.e(TAG, "Error while seeding database", e);
        } finally {
            if (!isInsideTransaction) {
                db.endTransaction();
            }
        }
    }

    private static long insertRole(SQLiteDatabase db, String name, String description) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", description);
        return db.insert("roles", null, cv);
    }

    private static long insertUser(SQLiteDatabase db, String username, String hash, String salt,
                                  String fullName, String email, String phone, long roleId,
                                  int active, String createdAt) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password_hash", hash);
        cv.put("password_salt", salt);
        cv.put("full_name", fullName);
        cv.put("email", email);
        cv.put("phone", phone);
        cv.put("role_id", roleId);
        cv.put("active", active);
        cv.put("created_at", createdAt);
        return db.insert("users", null, cv);
    }

    private static long insertSubject(SQLiteDatabase db, String code, String name, String description, int active) {
        ContentValues cv = new ContentValues();
        cv.put("code", code);
        cv.put("name", name);
        cv.put("description", description);
        cv.put("active", active);
        return db.insert("subjects", null, cv);
    }

    private static long insertTeacher(SQLiteDatabase db, String fullName, String phone, String email,
                                     String specialization, String degree, int experience, int active) {
        ContentValues cv = new ContentValues();
        cv.put("full_name", fullName);
        cv.put("phone", phone);
        cv.put("email", email);
        cv.put("specialization", specialization);
        cv.put("degree", degree);
        cv.put("experience", experience);
        cv.put("active", active);
        return db.insert("teachers", null, cv);
    }

    private static long insertRoom(SQLiteDatabase db, String name, int capacity, String location, int active) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("capacity", capacity);
        cv.put("location", location);
        cv.put("active", active);
        return db.insert("rooms", null, cv);
    }

    private static long insertClass(SQLiteDatabase db, String code, String name, long subjectId,
                                   long teacherId, long roomId, int maxStudents, double tuitionFee,
                                   String startDate, String endDate, String status) {
        ContentValues cv = new ContentValues();
        cv.put("code", code);
        cv.put("name", name);
        cv.put("subject_id", subjectId);
        cv.put("teacher_id", teacherId);
        cv.put("room_id", roomId);
        cv.put("max_students", maxStudents);
        cv.put("tuition_fee", tuitionFee);
        cv.put("start_date", startDate);
        cv.put("end_date", endDate);
        cv.put("status", status);
        return db.insert("classes", null, cv);
    }

    private static long insertSchedule(SQLiteDatabase db, long classId, int dayOfWeek,
                                      String startTime, String endTime, long roomId) {
        ContentValues cv = new ContentValues();
        cv.put("class_id", classId);
        cv.put("day_of_week", dayOfWeek);
        cv.put("start_time", startTime);
        cv.put("end_time", endTime);
        cv.put("room_id", roomId);
        return db.insert("schedules", null, cv);
    }

    private static long insertStudent(SQLiteDatabase db, String code, String fullName, String dob,
                                     String gender, String phone, String parentName, String parentPhone,
                                     String school, String gradeLevel, String address, int active, String createdAt) {
        ContentValues cv = new ContentValues();
        cv.put("student_code", code);
        cv.put("full_name", fullName);
        cv.put("date_of_birth", dob);
        cv.put("gender", gender);
        cv.put("phone", phone);
        cv.put("parent_name", parentName);
        cv.put("parent_phone", parentPhone);
        cv.put("school", school);
        cv.put("grade_level", gradeLevel);
        cv.put("address", address);
        cv.put("active", active);
        cv.put("created_at", createdAt);
        return db.insert("students", null, cv);
    }

    private static long insertClassStudent(SQLiteDatabase db, long classId, long studentId,
                                          String enrolledDate, String status) {
        ContentValues cv = new ContentValues();
        cv.put("class_id", classId);
        cv.put("student_id", studentId);
        cv.put("enrolled_date", enrolledDate);
        cv.put("status", status);
        return db.insert("class_students", null, cv);
    }

    private static long insertAttendance(SQLiteDatabase db, long classId, long studentId,
                                        String date, String status, String note) {
        ContentValues cv = new ContentValues();
        cv.put("class_id", classId);
        cv.put("student_id", studentId);
        cv.put("attendance_date", date);
        cv.put("status", status);
        cv.put("note", note);
        return db.insert("attendance", null, cv);
    }

    private static void seedInvoiceWithPayments(SQLiteDatabase db, String invoiceCode, long studentId,
                                               long classId, double amount, String dueDate,
                                               String monthYear, String note, String createdAt,
                                               long receivedBy, double paymentAmount,
                                               String paymentDate, String paymentMethod) {
        ContentValues cvInvoice = new ContentValues();
        cvInvoice.put("invoice_code", invoiceCode);
        cvInvoice.put("student_id", studentId);
        cvInvoice.put("class_id", classId);
        cvInvoice.put("amount", amount);
        cvInvoice.put("due_date", dueDate);
        cvInvoice.put("month_year", monthYear);
        cvInvoice.put("note", note);
        cvInvoice.put("created_at", createdAt);
        long invoiceId = db.insert("tuition_invoices", null, cvInvoice);

        if (paymentAmount > 0 && paymentDate != null && paymentMethod != null) {
            ContentValues cvPayment = new ContentValues();
            cvPayment.put("invoice_id", invoiceId);
            cvPayment.put("amount", paymentAmount);
            cvPayment.put("payment_date", paymentDate);
            cvPayment.put("payment_method", paymentMethod);
            cvPayment.put("note", "Thanh toán cho hóa đơn " + invoiceCode);
            cvPayment.put("received_by", receivedBy);
            db.insert("tuition_payments", null, cvPayment);
        }
    }

    private static long insertExamResult(SQLiteDatabase db, long studentId, long classId,
                                        String examName, String examDate, double score,
                                        double maxScore, String note) {
        ContentValues cv = new ContentValues();
        cv.put("student_id", studentId);
        cv.put("class_id", classId);
        cv.put("exam_name", examName);
        cv.put("exam_date", examDate);
        cv.put("score", score);
        cv.put("max_score", maxScore);
        cv.put("note", note);
        return db.insert("exam_results", null, cv);
    }
}
