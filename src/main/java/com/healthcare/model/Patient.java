package com.healthcare.model;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

/**
 * Concrete implementation of Patient user.
 * Encapsulates demographic, contact, and medical baseline information.
 */
public class Patient extends User {
    private Integer age;
    private String gender;
    private String phone;
    private String bloodGroup;
    private String address;
    private String emergencyContact;

    public Patient() {
        setRole(Role.PATIENT);
    }

    public Patient(Integer id, String name, String email, String passwordHash, Timestamp createdAt,
                   Integer age, String gender, String phone, String bloodGroup) {
        super(id, name, email, passwordHash, Role.PATIENT, createdAt);
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
    }

    @Override
    public String getDashboardPath() {
        return "/patient/dashboard";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
                "APPOINTMENT_BOOK",
                "APPOINTMENT_RESCHEDULE",
                "APPOINTMENT_CANCEL",
                "MEDICAL_HISTORY_VIEW",
                "FEEDBACK_SUBMIT"
        );
    }

    // Getters and Setters
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
