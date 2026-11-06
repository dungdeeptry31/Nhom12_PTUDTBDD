package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing an Exam / Quiz Result for a student in a class (Kết quả học tập).
 */
public class ExamResult implements Serializable {
    private long id;
    private long studentId;
    private long classId;
    private String examName;
    private String examDate;
    private double score;
    private double maxScore;
    private String note;

    // Joined fields
    private String studentName;
    private String studentCode;
    private String className;

    public ExamResult() {
    }

    public ExamResult(long id, long studentId, long classId, String examName,
                      String examDate, double score, double maxScore, String note) {
        this.id = id;
        this.studentId = studentId;
        this.classId = classId;
        this.examName = examName;
        this.examDate = examDate;
        this.score = score;
        this.maxScore = maxScore;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
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
