package com.healthcare.model;

/**
 * Enumeration representing lifecycle statuses for appointments.
 */
public enum AppointmentStatus {
    PENDING("Pending", "status-warning"),
    CONFIRMED("Confirmed", "status-success"),
    COMPLETED("Completed", "status-info"),
    CANCELLED("Cancelled", "status-danger"),
    RESCHEDULED("Rescheduled", "status-peach");

    private final String label;
    private final String cssClass;

    AppointmentStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getDisplayName() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public static AppointmentStatus fromString(String statusStr) {
        if (statusStr == null) return null;
        for (AppointmentStatus s : values()) {
            if (s.name().equalsIgnoreCase(statusStr.trim())) {
                return s;
            }
        }
        return null;
    }
}
