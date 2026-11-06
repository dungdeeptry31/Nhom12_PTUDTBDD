package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing a Class/Course (Lớp học).
 * Named ClassRoom to avoid conflict with Java keyword 'class'.
 */
public class ClassRoom implements Serializable {
    private long id;
    private String code;
    private String name;
    private long subjectId;
    private long teacherId;
    private long roomId;
    private int maxStudents;
    private double tuitionFee;
    private String startDate;
    private String endDate;
    private String status;

    // Joined fields
    private String subjectName;
    private String teacherName;
    private String roomName;
    private int currentStudents;

    public ClassRoom() {
    }

    public ClassRoom(long id, String code, String name, long subjectId, long teacherId,
                     long roomId, int maxStudents, double tuitionFee,
                     String startDate, String endDate, String status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.roomId = roomId;
        this.maxStudents = maxStudents;
        this.tuitionFee = tuitionFee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(long subjectId) {
        this.subjectId = subjectId;
    }

    public long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }

    public double getTuitionFee() {
        return tuitionFee;
    }

    public void setTuitionFee(double tuitionFee) {
        this.tuitionFee = tuitionFee;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCurrentStudents() {
        return currentStudents;
    }

    public void setCurrentStudents(int currentStudents) {
        this.currentStudents = currentStudents;
    }

    public boolean isFull() {
        return currentStudents >= maxStudents;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
