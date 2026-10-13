package com.healthcare.model;

/**
 * Enumeration representing user roles in the system.
 */
public enum Role {
    ADMIN("Administrator", "/admin/dashboard"),
    DOCTOR("Doctor", "/doctor/dashboard"),
    PATIENT("Patient", "/patient/dashboard");

    private final String displayName;
    private final String defaultDashboard;

    Role(String displayName, String defaultDashboard) {
        this.displayName = displayName;
        this.defaultDashboard = defaultDashboard;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultDashboard() {
        return defaultDashboard;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return null;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(roleStr.trim())) {
                return r;
            }
        }
        return null;
    }
}
