package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing an Attendance record (Điểm danh).
 */
public class Attendance implements Serializable {
    private long id;
    private long classId;
    private long studentId;
    private String attendanceDate; // yyyy-MM-dd
    private String status;         // PRESENT, ABSENT, LATE
    private String note;

    // Joined fields
    private String studentName;
    private String studentCode;
    private String className;

    public Attendance() {
    }

    public Attendance(long id, long classId, long studentId, String attendanceDate, String status, String note) {
        this.id = id;
        this.classId = classId;
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
