package com.healthcare.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

/**
 * Concrete implementation of Doctor user.
 * Encapsulates clinical credentials, specialization, fee, and schedule attributes.
 */
public class Doctor extends User {
    private String specialization;
    private int experience;
    private BigDecimal consultationFee = BigDecimal.valueOf(50.00);
    private String roomNumber;
    private String bio;

    // Derived/Transient fields for UI display & analytics
    private double averageRating;
    private int totalReviews;

    public Doctor() {
        setRole(Role.DOCTOR);
    }

    public Doctor(Integer id, String name, String email, String passwordHash, Timestamp createdAt,
                  String specialization, int experience, BigDecimal consultationFee) {
        super(id, name, email, passwordHash, Role.DOCTOR, createdAt);
        this.specialization = specialization;
        this.experience = experience;
        this.consultationFee = consultationFee;
    }

    @Override
    public String getDashboardPath() {
        return "/doctor/dashboard";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
                "SCHEDULE_MANAGE",
                "PATIENT_RECORDS_VIEW",
                "PATIENT_RECORDS_UPDATE",
                "APPOINTMENT_ACTION",
                "FEEDBACK_VIEW"
        );
    }

    // Getters and Setters
    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public double getRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public int getReviewCount() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }
}
