package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing an Enrollment of a student into a class (Bảng trung gian class_students).
 */
public class Enrollment implements Serializable {
    private long id;
    private long classId;
    private long studentId;
    private String enrolledDate;
    private String status;

    // Joined fields
    private String studentName;
    private String studentCode;
    private String studentPhone;
    private String studentGradeLevel;
    private String className;
    private String classCode;

    public Enrollment() {
    }

    public Enrollment(long id, long classId, long studentId, String enrolledDate, String status) {
        this.id = id;
        this.classId = classId;
        this.studentId = studentId;
        this.enrolledDate = enrolledDate;
        this.status = status;
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

    public String getEnrolledDate() {
        return enrolledDate;
    }

    public void setEnrolledDate(String enrolledDate) {
        this.enrolledDate = enrolledDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public String getStudentGradeLevel() {
        return studentGradeLevel;
    }

    public void setStudentGradeLevel(String studentGradeLevel) {
        this.studentGradeLevel = studentGradeLevel;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }
}
