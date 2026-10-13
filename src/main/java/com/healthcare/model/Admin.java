package com.healthcare.model;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

/**
 * Concrete implementation of Admin user.
 */
public class Admin extends User {

    public Admin() {
        setRole(Role.ADMIN);
    }

    public Admin(Integer id, String name, String email, String passwordHash, Timestamp createdAt) {
        super(id, name, email, passwordHash, Role.ADMIN, createdAt);
    }

    @Override
    public String getDashboardPath() {
        return "/admin/dashboard";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
                "USER_MANAGE",
                "APPOINTMENT_MANAGE",
                "SETTINGS_MANAGE",
                "ANALYTICS_VIEW",
                "AUDIT_VIEW"
        );
    }
}
