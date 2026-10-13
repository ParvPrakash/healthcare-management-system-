package com.healthcare.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interface contract for schedulable/bookable entities.
 */
public interface Bookable {
    Integer getDoctorId();
    LocalDate getSlotDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
    boolean isAvailable();

    /**
     * Default method to generate a unique string signature for the booking slot.
     */
    default String getSlotSignature() {
        return getDoctorId() + "_" + getSlotDate() + "_" + getStartTime();
    }
}
