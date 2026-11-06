package com.educenter.app.model;

import com.educenter.app.utils.Constants;

import java.io.Serializable;

/**
 * Model representing a Tuition Invoice (Hóa đơn học phí).
 */
public class TuitionInvoice implements Serializable {
    private long id;
    private String invoiceCode;
    private long studentId;
    private long classId;
    private double amount;
    private String dueDate;
    private String monthYear; // MM/yyyy
    private String note;
    private String createdAt;

    // Joined / Calculated fields
    private double paidAmount;
    private String studentName;
    private String studentCode;
    private String className;

    public TuitionInvoice() {
    }

    public TuitionInvoice(long id, String invoiceCode, long studentId, long classId,
                          double amount, String dueDate, String monthYear,
                          String note, String createdAt) {
        this.id = id;
        this.invoiceCode = invoiceCode;
        this.studentId = studentId;
        this.classId = classId;
        this.amount = amount;
        this.dueDate = dueDate;
        this.monthYear = monthYear;
        this.note = note;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getRemainingAmount() {
        double rem = amount - paidAmount;
        return rem > 0 ? rem : 0.0;
    }

    public String getStatus() {
        if (paidAmount >= amount && amount > 0) {
            return Constants.TUITION_PAID;
        } else if (paidAmount > 0) {
            return Constants.TUITION_PARTIAL;
        } else {
            return Constants.TUITION_UNPAID;
        }
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
