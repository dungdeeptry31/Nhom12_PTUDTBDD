package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing a Weekly Class Schedule item (Lịch học).
 */
public class Schedule implements Serializable {
    private long id;
    private long classId;
    private int dayOfWeek; // 1 = Sunday, 2 = Monday, ..., 7 = Saturday
    private String startTime; // HH:mm
    private String endTime;   // HH:mm
    private long roomId;

    // Joined fields
    private String className;
    private String classCode;
    private String roomName;
    private String teacherName;

    public Schedule() {
    }

    public Schedule(long id, long classId, int dayOfWeek, String startTime, String endTime, long roomId) {
        this.id = id;
        this.classId = classId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomId = roomId;
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

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
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

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
