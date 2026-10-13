package com.healthcare.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model representing an electronic medical record (EMR).
 */
public class MedicalRecord {
    private Integer id;
    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private String diagnosis;
    private String prescription;
    private String vitalsBloodPressure;
    private String vitalsHeartRate;
    private String vitalsTemperature;
    private String notes;
    private LocalDate recordDate;
    private Timestamp createdAt;

    // Joined fields for display
    private String doctorName;
    private String doctorSpecialization;
    private String patientName;

    public MedicalRecord() {}

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getVitalsBloodPressure() {
        return vitalsBloodPressure;
    }

    public void setVitalsBloodPressure(String vitalsBloodPressure) {
        this.vitalsBloodPressure = vitalsBloodPressure;
    }

    public String getVitalsHeartRate() {
        return vitalsHeartRate;
    }

    public void setVitalsHeartRate(String vitalsHeartRate) {
        this.vitalsHeartRate = vitalsHeartRate;
    }

    public String getVitalsTemperature() {
        return vitalsTemperature;
    }

    public void setVitalsTemperature(String vitalsTemperature) {
        this.vitalsTemperature = vitalsTemperature;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecord that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
