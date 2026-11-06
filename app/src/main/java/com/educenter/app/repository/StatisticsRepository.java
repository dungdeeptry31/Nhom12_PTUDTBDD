package com.educenter.app.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.educenter.app.EduCenterApplication;
import com.educenter.app.data.local.DatabaseHelper;
import com.educenter.app.utils.CurrencyUtils;
import com.educenter.app.utils.DateUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Repository for Dashboard statistics, Charts data, and Analytics reporting.
 */
public class StatisticsRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public StatisticsRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static class DashboardOverview implements Serializable {
        public int totalStudents;
        public int totalOpenClasses;
        public int totalActiveTeachers;
        public double currentMonthRevenue;
        public double totalDebt;
        public int unpaidInvoicesCount;
    }

    public static class MonthlyRevenue implements Serializable {
        public String monthLabel; // e.g. "06/26" or "T6"
        public String fullMonthYear; // e.g. "06/2026"
        public double revenue;

        public MonthlyRevenue(String monthLabel, String fullMonthYear, double revenue) {
            this.monthLabel = monthLabel;
            this.fullMonthYear = fullMonthYear;
            this.revenue = revenue;
        }
    }

    public static class TuitionDistribution implements Serializable {
        public int paidCount;
        public int partialCount;
        public int unpaidCount;
        public double paidTotal;
        public double debtTotal;
    }

    public static class AttendanceSummary implements Serializable {
        public int presentCount;
        public int absentCount;
        public int lateCount;
        public int totalCount;

        public float getPresentRate() {
            return totalCount == 0 ? 0 : (presentCount * 100f / totalCount);
        }

        public float getAbsentRate() {
            return totalCount == 0 ? 0 : (absentCount * 100f / totalCount);
        }

        public float getLateRate() {
            return totalCount == 0 ? 0 : (lateCount * 100f / totalCount);
        }
    }

    public static class RecentActivity implements Serializable {
        public String title;
        public String description;
        public String time;
        public String iconType; // "PAYMENT", "STUDENT", "ATTENDANCE"

        public RecentActivity(String title, String description, String time, String iconType) {
            this.title = title;
            this.description = description;
            this.time = time;
            this.iconType = iconType;
        }
    }

    public void getDashboardOverview(DataCallback<DashboardOverview> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            DashboardOverview overview = new DashboardOverview();
            try {
                // 1. Total students
                Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_STUDENTS + " WHERE active = 1", null);
                if (c1.moveToFirst()) overview.totalStudents = c1.getInt(0);
                c1.close();

                // 2. Total open classes
                Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLASSES + " WHERE status = 'OPEN'", null);
                if (c2.moveToFirst()) overview.totalOpenClasses = c2.getInt(0);
                c2.close();

                // 3. Total active teachers
                Cursor c3 = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEACHERS + " WHERE active = 1", null);
                if (c3.moveToFirst()) overview.totalActiveTeachers = c3.getInt(0);
                c3.close();

                // 4. Current month revenue (sum of payments in current month)
                String currentMonth = DateUtils.getCurrentMonthYear(); // MM/yyyy
                // Payments have payment_date formatted as yyyy-MM-dd
                String currentYearMonthDb = DateUtils.getTodayDb().substring(0, 7); // yyyy-MM
                Cursor c4 = db.rawQuery(
                        "SELECT COALESCE(SUM(amount), 0.0) FROM " + DatabaseHelper.TABLE_TUITION_PAYMENTS
                                + " WHERE payment_date LIKE ?",
                        new String[]{currentYearMonthDb + "%"}
                );
                if (c4.moveToFirst()) overview.currentMonthRevenue = c4.getDouble(0);
                c4.close();

                // 5. Total debt calculation: SUM(inv.amount) - SUM(payments.amount)
                Cursor c5 = db.rawQuery("SELECT COALESCE(SUM(amount), 0.0) FROM " + DatabaseHelper.TABLE_TUITION_INVOICES, null);
                double totalInvoiced = 0.0;
                if (c5.moveToFirst()) totalInvoiced = c5.getDouble(0);
                c5.close();

                Cursor c6 = db.rawQuery("SELECT COALESCE(SUM(amount), 0.0) FROM " + DatabaseHelper.TABLE_TUITION_PAYMENTS, null);
                double totalPaid = 0.0;
                if (c6.moveToFirst()) totalPaid = c6.getDouble(0);
                c6.close();

                overview.totalDebt = Math.max(0, totalInvoiced - totalPaid);

                mainHandler.post(() -> callback.onSuccess(overview));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi khi tải thống kê tổng quan: " + e.getMessage()));
            }
        });
    }

    /**
     * Retrieves actual revenue for the last 6 months from tuition_payments table.
     */
    public void getRevenueLast6Months(DataCallback<List<MonthlyRevenue>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<MonthlyRevenue> list = new ArrayList<>();
            try {
                // Generate last 6 months in chronological order
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -5); // Start 5 months ago

                for (int i = 0; i < 6; i++) {
                    int month = cal.get(Calendar.MONTH) + 1; // 1..12
                    int year = cal.get(Calendar.YEAR);
                    String monthDbPrefix = String.format(java.util.Locale.US, "%04d-%02d", year, month);
                    String monthLabel = String.format(java.util.Locale.US, "T%d", month);
                    String fullMonthYear = String.format(java.util.Locale.US, "%02d/%04d", month, year);

                    Cursor cursor = db.rawQuery(
                            "SELECT COALESCE(SUM(amount), 0.0) FROM " + DatabaseHelper.TABLE_TUITION_PAYMENTS
                                    + " WHERE payment_date LIKE ?",
                            new String[]{monthDbPrefix + "%"}
                    );
                    double rev = 0.0;
                    if (cursor.moveToFirst()) {
                        rev = cursor.getDouble(0);
                    }
                    cursor.close();

                    list.add(new MonthlyRevenue(monthLabel, fullMonthYear, rev));
                    cal.add(Calendar.MONTH, 1);
                }

                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi khi tính biểu đồ doanh thu: " + e.getMessage()));
            }
        });
    }

    public void getTuitionDistribution(DataCallback<TuitionDistribution> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            TuitionDistribution dist = new TuitionDistribution();
            try {
                String sql = "SELECT inv.amount, "
                        + "COALESCE((SELECT SUM(p.amount) FROM " + DatabaseHelper.TABLE_TUITION_PAYMENTS + " p WHERE p.invoice_id = inv.id), 0.0) as paid "
                        + "FROM " + DatabaseHelper.TABLE_TUITION_INVOICES + " inv";

                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    double amount = cursor.getDouble(0);
                    double paid = cursor.getDouble(1);

                    dist.paidTotal += paid;
                    if (paid >= amount && amount > 0) {
                        dist.paidCount++;
                    } else if (paid > 0) {
                        dist.partialCount++;
                        dist.debtTotal += (amount - paid);
                    } else {
                        dist.unpaidCount++;
                        dist.debtTotal += amount;
                    }
                }
                cursor.close();
                mainHandler.post(() -> callback.onSuccess(dist));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi tính phân bố học phí: " + e.getMessage()));
            }
        });
    }

    public void getStudentsByGrade(DataCallback<Map<String, Integer>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Map<String, Integer> map = new LinkedHashMap<>();
            try {
                String sql = "SELECT grade_level, COUNT(*) as count FROM " + DatabaseHelper.TABLE_STUDENTS
                        + " WHERE active = 1 GROUP BY grade_level ORDER BY grade_level ASC";
                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    String grade = "Khối " + cursor.getString(0);
                    int count = cursor.getInt(1);
                    map.put(grade, count);
                }
                cursor.close();
                mainHandler.post(() -> callback.onSuccess(map));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi thống kê học sinh theo khối: " + e.getMessage()));
            }
        });
    }

    public void getAttendanceSummary(DataCallback<AttendanceSummary> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            AttendanceSummary summary = new AttendanceSummary();
            try {
                String sql = "SELECT status, COUNT(*) as count FROM " + DatabaseHelper.TABLE_ATTENDANCE + " GROUP BY status";
                Cursor cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    String status = cursor.getString(0);
                    int count = cursor.getInt(1);
                    summary.totalCount += count;
                    if ("PRESENT".equalsIgnoreCase(status)) {
                        summary.presentCount += count;
                    } else if ("ABSENT".equalsIgnoreCase(status)) {
                        summary.absentCount += count;
                    } else if ("LATE".equalsIgnoreCase(status)) {
                        summary.lateCount += count;
                    }
                }
                cursor.close();
                mainHandler.post(() -> callback.onSuccess(summary));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi tính chuyên cần: " + e.getMessage()));
            }
        });
    }

    public void getRecentActivities(DataCallback<List<RecentActivity>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<RecentActivity> activities = new ArrayList<>();
            try {
                // 1. Recent payments
                String paySql = "SELECT p.amount, p.payment_date, s.full_name as student_name, inv.invoice_code "
                        + "FROM " + DatabaseHelper.TABLE_TUITION_PAYMENTS + " p "
                        + "JOIN " + DatabaseHelper.TABLE_TUITION_INVOICES + " inv ON p.invoice_id = inv.id "
                        + "JOIN " + DatabaseHelper.TABLE_STUDENTS + " s ON inv.student_id = s.id "
                        + "ORDER BY p.id DESC LIMIT 3";
                Cursor cPay = db.rawQuery(paySql, null);
                while (cPay.moveToNext()) {
                    String studentName = cPay.getString(cPay.getColumnIndexOrThrow("student_name"));
                    double amount = cPay.getDouble(cPay.getColumnIndexOrThrow("amount"));
                    String date = cPay.getString(cPay.getColumnIndexOrThrow("payment_date"));
                    activities.add(new RecentActivity(
                            "Thu học phí thành công",
                            studentName + " đã nộp " + CurrencyUtils.formatVnd(amount),
                            DateUtils.toDisplayDate(date),
                            "PAYMENT"
                    ));
                }
                cPay.close();

                // 2. Recent students added
                String stuSql = "SELECT full_name, created_at, grade_level FROM " + DatabaseHelper.TABLE_STUDENTS
                        + " ORDER BY id DESC LIMIT 2";
                Cursor cStu = db.rawQuery(stuSql, null);
                while (cStu.moveToNext()) {
                    String name = cStu.getString(cStu.getColumnIndexOrThrow("full_name"));
                    String grade = cStu.getString(cStu.getColumnIndexOrThrow("grade_level"));
                    String createdAt = cStu.getString(cStu.getColumnIndexOrThrow("created_at"));
                    activities.add(new RecentActivity(
                            "Tiếp nhận học sinh mới",
                            name + " (Khối " + grade + ") đã đăng ký học",
                            DateUtils.toDisplayDate(createdAt.split(" ")[0]),
                            "STUDENT"
                    ));
                }
                cStu.close();

                mainHandler.post(() -> callback.onSuccess(activities));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi tải hoạt động gần đây: " + e.getMessage()));
            }
        });
    }
}
