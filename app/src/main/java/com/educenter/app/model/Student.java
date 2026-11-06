package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing a Student (Học sinh).
 */
public class Student implements Serializable {
    private long id;
    private String studentCode;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String phone;
    private String parentName;
    private String parentPhone;
    private String school;
    private String gradeLevel;
    private String address;
    private int active;
    private String createdAt;

    // Joined fields
    private int enrolledClassesCount;

    public Student() {
    }

    public Student(long id, String studentCode, String fullName, String dateOfBirth,
                   String gender, String phone, String parentName, String parentPhone,
                   String school, String gradeLevel, String address, int active,
                   String createdAt) {
        this.id = id;
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.school = school;
        this.gradeLevel = gradeLevel;
        this.address = address;
        this.active = active;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active == 1;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getEnrolledClassesCount() {
        return enrolledClassesCount;
    }

    public void setEnrolledClassesCount(int enrolledClassesCount) {
        this.enrolledClassesCount = enrolledClassesCount;
    }

    @Override
    public String toString() {
        return fullName + " (" + studentCode + ")";
    }
}
