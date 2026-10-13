package com.healthcare.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * Abstract Base Model for all system users.
 * Implements Authenticatable and Notifiable interfaces.
 * Provides polymorphic methods getDashboardPath() and getPermissions().
 */
public abstract class User implements Authenticatable, Notifiable, Comparable<User> {
    private Integer id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private String status = "ACTIVE";
    private int failedAttempts = 0;
    private Timestamp lockedUntil;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {}

    public User(Integer id, String name, String email, String passwordHash, Role role, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    /**
     * Abstract method defining the user's home dashboard route.
     * Demonstrates runtime polymorphism across Admin, Doctor, and Patient.
     */
    public abstract String getDashboardPath();

    /**
     * Abstract method defining granular permission keys.
     * Demonstrates runtime polymorphism.
     */
    public abstract List<String> getPermissions();

    @Override
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    @Override
    public int compareTo(User o) {
        if (o == null || this.name == null) return 1;
        return this.name.compareToIgnoreCase(o.name);
    }

    // Getters and Setters
    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Timestamp getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Timestamp lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
