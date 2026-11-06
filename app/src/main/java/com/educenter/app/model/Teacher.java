package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing a Teacher (Giáo viên).
 */
public class Teacher implements Serializable {
    private long id;
    private String fullName;
    private String phone;
    private String email;
    private String specialization;
    private String degree;
    private int experience;
    private int active;

    // Joined fields
    private int activeClassesCount;

    public Teacher() {
    }

    public Teacher(long id, String fullName, String phone, String email,
                   String specialization, String degree, int experience, int active) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.specialization = specialization;
        this.degree = degree;
        this.experience = experience;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
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

    public int getActiveClassesCount() {
        return activeClassesCount;
    }

    public void setActiveClassesCount(int activeClassesCount) {
        this.activeClassesCount = activeClassesCount;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
