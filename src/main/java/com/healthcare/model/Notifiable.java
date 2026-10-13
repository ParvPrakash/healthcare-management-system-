package com.healthcare.model;

/**
 * Interface contract for users or entities capable of receiving notifications.
 */
public interface Notifiable {
    Integer getId();
    String getName();
    String getEmail();

    /**
     * Default method to compose a friendly greeting for notifications.
     */
    default String getRecipientGreeting() {
        return "Hello " + (getName() != null ? getName() : "User");
    }
}
