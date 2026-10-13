package com.healthcare.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Appointment model representing scheduled clinical visits.
 * Implements Reportable for CSV exports and PDF generation.
 */
public class Appointment implements Reportable, Comparable<Appointment> {
    private Integer id;
    private Integer doctorId;
    private Integer patientId;
    private Integer slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status = AppointmentStatus.CONFIRMED;
    private String reason;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined / Presentation fields
    private String doctorName;
    private String doctorSpecialization;
    private BigDecimal doctorFee;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private String patientBloodGroup;
    private Integer feedbackRating;
    private String feedbackComment;

    public Appointment() {}

    @Override
    public String getReportIdentifier() {
        return "APP-" + id;
    }

    @Override
    public String getReportTitle() {
        return "Appointment with Dr. " + (doctorName != null ? doctorName : doctorId) + " on " + appointmentDate;
    }

    @Override
    public Map<String, Object> toReportMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("doctorName", doctorName);
        map.put("patientName", patientName);
        map.put("date", appointmentDate != null ? appointmentDate.toString() : "");
        map.put("time", getFormattedTime());
        map.put("status", status != null ? status.name() : "");
        map.put("reason", reason);
        return map;
    }

    @Override
    public String toCsvRow() {
        return String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                id,
                doctorName != null ? doctorName : "",
                patientName != null ? patientName : "",
                appointmentDate != null ? appointmentDate.toString() : "",
                getFormattedTime(),
                status != null ? status.name() : "",
                reason != null ? reason.replace("\"", "\"\"") : "");
    }

    @Override
    public int compareTo(Appointment o) {
        if (o == null) return 1;
        int dateCmp = this.appointmentDate.compareTo(o.appointmentDate);
        if (dateCmp != 0) return dateCmp;
        return this.startTime.compareTo(o.startTime);
    }

    public String getFormattedTime() {
        if (startTime == null || endTime == null) return "";
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

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getSlotId() {
        return slotId;
    }

    public void setSlotId(Integer slotId) {
        this.slotId = slotId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public BigDecimal getDoctorFee() {
        return doctorFee;
    }

    public void setDoctorFee(BigDecimal doctorFee) {
        this.doctorFee = doctorFee;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientBloodGroup() {
        return patientBloodGroup;
    }

    public void setPatientBloodGroup(String patientBloodGroup) {
        this.patientBloodGroup = patientBloodGroup;
    }

    public Integer getFeedbackRating() {
        return feedbackRating;
    }

    public void setFeedbackRating(Integer feedbackRating) {
        this.feedbackRating = feedbackRating;
    }

    public String getFeedbackComment() {
        return feedbackComment;
    }

    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }

    public String getStatusBadgeClass() {
        return status != null ? status.getCssClass() : "status-info";
    }

    public String getSpecialization() {
        return doctorSpecialization != null ? doctorSpecialization : "";
    }

    public BigDecimal getConsultationFee() {
        return doctorFee != null ? doctorFee : BigDecimal.valueOf(50.00);
    }

    public String getReasonForVisit() {
        return reason != null ? reason : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
