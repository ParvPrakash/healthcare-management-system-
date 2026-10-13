package com.healthcare.model;

/**
 * Interface contract for entities that authenticate into the system.
 */
public interface Authenticatable {
    String getEmail();
    String getPasswordHash();
    Role getRole();
    boolean isActive();

    /**
     * Default method to check if user has permission for a specific feature.
     */
    default boolean hasPermission(String permission) {
        if (permission == null) return false;
        if (getRole() == Role.ADMIN) return true;
        return false;
    }
}
