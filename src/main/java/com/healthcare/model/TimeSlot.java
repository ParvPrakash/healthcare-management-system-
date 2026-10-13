package com.healthcare.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Concrete time slot entity for a doctor on a specific date.
 * Implements Bookable and Comparable for ordered slot listings.
 */
public class TimeSlot implements Bookable, Comparable<TimeSlot> {
    private Integer id;
    private Integer doctorId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isBooked;
    private int version;

    // Transient helper fields
    private String doctorName;
    private String doctorSpecialization;

    public TimeSlot() {}

    public TimeSlot(Integer id, Integer doctorId, LocalDate slotDate, LocalTime startTime, LocalTime endTime, boolean isBooked) {
        this.id = id;
        this.doctorId = doctorId;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = isBooked;
    }

    @Override
    public boolean isAvailable() {
        return !isBooked;
    }

    @Override
    public int compareTo(TimeSlot o) {
        if (o == null) return 1;
        int dateCmp = this.slotDate.compareTo(o.slotDate);
        if (dateCmp != 0) return dateCmp;
        return this.startTime.compareTo(o.startTime);
    }

    public String getFormattedTimeRange() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
        return startTime.format(fmt) + " - " + endTime.format(fmt);
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public LocalDate getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(LocalDate slotDate) {
        this.slotDate = slotDate;
    }

    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot timeSlot)) return false;
        return Objects.equals(doctorId, timeSlot.doctorId) &&
                Objects.equals(slotDate, timeSlot.slotDate) &&
                Objects.equals(startTime, timeSlot.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId, slotDate, startTime);
    }
}
