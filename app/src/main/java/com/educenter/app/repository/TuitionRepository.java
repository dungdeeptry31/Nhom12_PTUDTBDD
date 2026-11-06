package com.educenter.app.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.educenter.app.EduCenterApplication;
import com.educenter.app.data.local.DatabaseHelper;
import com.educenter.app.model.TuitionInvoice;
import com.educenter.app.model.TuitionPayment;
import com.educenter.app.utils.Constants;
import com.educenter.app.utils.CurrencyUtils;
import com.educenter.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for Tuition Invoices and Payment installments.
 */
public class TuitionRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public TuitionRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = EduCenterApplication.getInstance().getAppExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getAllInvoices(DataCallback<List<TuitionInvoice>> callback) {
        searchInvoices("", null, null, null, callback);
    }

    public void searchInvoices(String keyword, String statusFilter, String monthFilter, Long classFilter, DataCallback<List<TuitionInvoice>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT inv.*, s.full_name as student_name, s.student_code as student_code, c.name as class_name, ")
               .append("COALESCE((SELECT SUM(p.amount) FROM ").append(DatabaseHelper.TABLE_TUITION_PAYMENTS)
               .append(" p WHERE p.invoice_id = inv.id), 0.0) as paid_amount ")
               .append("FROM ").append(DatabaseHelper.TABLE_TUITION_INVOICES).append(" inv ")
               .append("JOIN ").append(DatabaseHelper.TABLE_STUDENTS).append(" s ON inv.student_id = s.id ")
               .append("JOIN ").append(DatabaseHelper.TABLE_CLASSES).append(" c ON inv.class_id = c.id ")
               .append("WHERE 1=1 ");

            List<String> args = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (inv.invoice_code LIKE ? OR s.full_name LIKE ? OR s.student_code LIKE ?) ");
                String kw = "%" + keyword.trim() + "%";
                args.add(kw);
                args.add(kw);
                args.add(kw);
            }

            if (monthFilter != null && !monthFilter.trim().isEmpty() && !monthFilter.equals("Tất cả")) {
                sql.append("AND inv.month_year = ? ");
                args.add(monthFilter.trim());
            }

            if (classFilter != null && classFilter > 0) {
                sql.append("AND inv.class_id = ? ");
                args.add(String.valueOf(classFilter));
            }

            sql.append("ORDER BY inv.id DESC");

            Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
            List<TuitionInvoice> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    TuitionInvoice inv = parseInvoice(cursor);

                    // Filter by computed status if requested
                    if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equals("Tất cả")) {
                        if (!statusFilter.equalsIgnoreCase(inv.getStatus())) {
                            continue;
                        }
                    }
                    list.add(inv);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi truy vấn hóa đơn: " + e.getMessage()));
            } finally {
                cursor.close();
            }
        });
    }

    public void getInvoiceById(long id, DataCallback<TuitionInvoice> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT inv.*, s.full_name as student_name, s.student_code as student_code, c.name as class_name, "
                    + "COALESCE((SELECT SUM(p.amount) FROM " + DatabaseHelper.TABLE_TUITION_PAYMENTS + " p WHERE p.invoice_id = inv.id), 0.0) as paid_amount "
                    + "FROM " + DatabaseHelper.TABLE_TUITION_INVOICES + " inv "
                    + "JOIN " + DatabaseHelper.TABLE_STUDENTS + " s ON inv.student_id = s.id "
                    + "JOIN " + DatabaseHelper.TABLE_CLASSES + " c ON inv.class_id = c.id "
                    + "WHERE inv.id = ?";

            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            try {
                if (cursor.moveToFirst()) {
                    TuitionInvoice inv = parseInvoice(cursor);
                    mainHandler.post(() -> callback.onSuccess(inv));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy hóa đơn."));
                }
            } finally {
                cursor.close();
            }
        });
    }

    public void createInvoice(TuitionInvoice invoice, DataCallback<Long> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check duplicate invoice_code
            Cursor checkCode = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TUITION_INVOICES + " WHERE invoice_code = ?",
                    new String[]{invoice.getInvoiceCode().trim()}
            );
            try {
                if (checkCode.moveToFirst() && checkCode.getInt(0) > 0) {
                    mainHandler.post(() -> callback.onError("Mã hóa đơn đã tồn tại!"));
                    return;
                }
            } finally {
                checkCode.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("invoice_code", invoice.getInvoiceCode().trim().toUpperCase());
            cv.put("student_id", invoice.getStudentId());
            cv.put("class_id", invoice.getClassId());
            cv.put("amount", invoice.getAmount());
            cv.put("due_date", invoice.getDueDate());
            cv.put("month_year", invoice.getMonthYear());
            cv.put("note", invoice.getNote() != null ? invoice.getNote() : "");
            cv.put("created_at", DateUtils.getCurrentDateTimeDb());

            long id = db.insert(DatabaseHelper.TABLE_TUITION_INVOICES, null, cv);
            if (id > 0) {
                mainHandler.post(() -> callback.onSuccess(id));
            } else {
                mainHandler.post(() -> callback.onError("Tạo hóa đơn học phí thất bại."));
            }
        });
    }

    /**
     * Records a payment installment.
     * Enforces TC19 (partial payment updates debt), TC20 (block if amount > remaining debt), TC21 (full payment).
     */
    public void recordPayment(TuitionPayment payment, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                // 1. Get invoice amount
                Cursor invCursor = db.rawQuery(
                        "SELECT amount FROM " + DatabaseHelper.TABLE_TUITION_INVOICES + " WHERE id = ?",
                        new String[]{String.valueOf(payment.getInvoiceId())}
                );
                double totalAmount = 0.0;
                if (invCursor.moveToFirst()) {
                    totalAmount = invCursor.getDouble(0);
                } else {
                    invCursor.close();
                    mainHandler.post(() -> callback.onError("Hóa đơn không tồn tại!"));
                    return;
                }
                invCursor.close();

                // 2. Get current paid amount
                Cursor paidCursor = db.rawQuery(
                        "SELECT COALESCE(SUM(amount), 0.0) FROM " + DatabaseHelper.TABLE_TUITION_PAYMENTS + " WHERE invoice_id = ?",
                        new String[]{String.valueOf(payment.getInvoiceId())}
                );
                double currentPaid = 0.0;
                if (paidCursor.moveToFirst()) {
                    currentPaid = paidCursor.getDouble(0);
                }
                paidCursor.close();

                double remainingDebt = totalAmount - currentPaid;

                // 3. Validation: payment amount must be > 0 and <= remainingDebt
                if (payment.getAmount() <= 0) {
                    mainHandler.post(() -> callback.onError("Số tiền thanh toán phải lớn hơn 0!"));
                    return;
                }

                // TC20: Check overflow
                if (payment.getAmount() > remainingDebt + 0.01) { // 0.01 epsilon
                    mainHandler.post(() -> callback.onError("Số tiền thanh toán ("
                            + CurrencyUtils.formatVnd(payment.getAmount())
                            + ") không được vượt quá số tiền còn nợ ("
                            + CurrencyUtils.formatVnd(remainingDebt) + ")!"));
                    return;
                }

                // 4. Insert payment
                ContentValues cv = new ContentValues();
                cv.put("invoice_id", payment.getInvoiceId());
                cv.put("amount", payment.getAmount());
                cv.put("payment_date", payment.getPaymentDate());
                cv.put("payment_method", payment.getPaymentMethod());
                cv.put("note", payment.getNote() != null ? payment.getNote() : "");
                cv.put("received_by", payment.getReceivedBy());

                long result = db.insert(DatabaseHelper.TABLE_TUITION_PAYMENTS, null, cv);
                if (result > 0) {
                    db.setTransactionSuccessful();
                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onError("Ghi nhận thanh toán thất bại."));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi khi thanh toán: " + e.getMessage()));
            } finally {
                db.endTransaction();
            }
        });
    }

    public void getPaymentsForInvoice(long invoiceId, DataCallback<List<TuitionPayment>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT p.*, u.full_name as receiver_name, inv.invoice_code, s.full_name as student_name "
                    + "FROM " + DatabaseHelper.TABLE_TUITION_PAYMENTS + " p "
                    + "JOIN " + DatabaseHelper.TABLE_USERS + " u ON p.received_by = u.id "
                    + "JOIN " + DatabaseHelper.TABLE_TUITION_INVOICES + " inv ON p.invoice_id = inv.id "
                    + "JOIN " + DatabaseHelper.TABLE_STUDENTS + " s ON inv.student_id = s.id "
                    + "WHERE p.invoice_id = ? "
                    + "ORDER BY p.id DESC";

            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(invoiceId)});
            List<TuitionPayment> list = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    TuitionPayment p = new TuitionPayment(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("invoice_id")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("payment_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("payment_method")),
                            cursor.getString(cursor.getColumnIndexOrThrow("note")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("received_by"))
                    );
                    p.setReceiverName(cursor.getString(cursor.getColumnIndexOrThrow("receiver_name")));
                    p.setInvoiceCode(cursor.getString(cursor.getColumnIndexOrThrow("invoice_code")));
                    p.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                    list.add(p);
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } finally {
                cursor.close();
            }
        });
    }

    private TuitionInvoice parseInvoice(Cursor cursor) {
        TuitionInvoice inv = new TuitionInvoice(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("invoice_code")),
                cursor.getLong(cursor.getColumnIndexOrThrow("student_id")),
                cursor.getLong(cursor.getColumnIndexOrThrow("class_id")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                cursor.getString(cursor.getColumnIndexOrThrow("due_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("month_year")),
                cursor.getString(cursor.getColumnIndexOrThrow("note")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
        );
        inv.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
        inv.setStudentCode(cursor.getString(cursor.getColumnIndexOrThrow("student_code")));
        inv.setClassName(cursor.getString(cursor.getColumnIndexOrThrow("class_name")));
        inv.setPaidAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("paid_amount")));
        return inv;
    }
}
