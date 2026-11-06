package com.educenter.app.model;

import java.io.Serializable;

/**
 * Model representing a System User (Account).
 */
public class User implements Serializable {
    private long id;
    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String fullName;
    private String email;
    private String phone;
    private long roleId;
    private int active;
    private String createdAt;

    // Joined fields
    private String roleName;

    public User() {
    }

    public User(long id, String username, String passwordHash, String passwordSalt,
                String fullName, String email, String phone, long roleId,
                int active, String createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleId = roleId;
        this.active = active;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
