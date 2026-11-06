package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing a Subject (Môn học).
 */
public class Subject implements Serializable {
    private long id;
    private String code;
    private String name;
    private String description;
    private int active;

    // Joined fields
    private int activeClassesCount;

    public Subject() {
    }

    public Subject(long id, String code, String name, String description, int active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return name;
    }
}
