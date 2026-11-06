package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing a Classroom/Room (Phòng học).
 */
public class Room implements Serializable {
    private long id;
    private String name;
    private int capacity;
    private String location;
    private int active;

    public Room() {
    }

    public Room(long id, String name, int capacity, String location, int active) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    @Override
    public String toString() {
        return name + " (" + capacity + " chỗ)";
    }
}
