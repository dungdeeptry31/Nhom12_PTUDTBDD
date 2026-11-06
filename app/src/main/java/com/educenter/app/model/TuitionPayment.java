package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing an installment or single Payment for a tuition invoice (Phiếu thu / Lần thanh toán).
 */
public class TuitionPayment implements Serializable {
    private long id;
    private long invoiceId;
    private double amount;
    private String paymentDate;
    private String paymentMethod; // CASH, TRANSFER
    private String note;
    private long receivedBy;

    // Joined fields
    private String receiverName;
    private String invoiceCode;
    private String studentName;

    public TuitionPayment() {
    }

    public TuitionPayment(long id, long invoiceId, double amount, String paymentDate,
                          String paymentMethod, String note, long receivedBy) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.note = note;
        this.receivedBy = receivedBy;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(long receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
