package com.healthcare.service;

import com.healthcare.dao.*;
import com.healthcare.exception.DatabaseException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.ValidationException;
import com.healthcare.model.*;
import com.healthcare.util.DBConnection;
import com.healthcare.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing user profiles, role assignments, and directory administration.
 * Demonstrates Bounded Generics (<T extends User>) and Streams API (§4.2).
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final DoctorDao doctorDao;
    private final PatientDao patientDao;
    private final AuditDao auditDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
        this.doctorDao = new DoctorDaoImpl();
        this.patientDao = new PatientDaoImpl();
        this.auditDao = new AuditDaoImpl();
    }

    public UserService(UserDao userDao, DoctorDao doctorDao, PatientDao patientDao, AuditDao auditDao) {
        this.userDao = userDao;
        this.doctorDao = doctorDao;
        this.patientDao = patientDao;
        this.auditDao = auditDao;
    }

    /**
     * Demonstrates Bounded Generics (<T extends User>) for polymorphic validation.
     */
    public <T extends User> T validateAndFormatUser(T user) {
        if (user != null && user.getName() != null) {
            user.setName(user.getName().trim());
        }
        if (user != null && user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }
        return user;
    }

    /**
     * Demonstrates Wildcard collections (List<? extends User>) with Java Stream filtering.
     */
    public List<? extends User> filterActiveUsers(List<? extends User> users) {
        if (users == null) return Collections.emptyList();
        return users.stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    public Optional<User> getUserById(int id) throws DatabaseException {
        return userDao.findById(id);
    }

    public List<User> searchUsers(String query, Role role, int page, int pageSize) throws DatabaseException {
        int offset = Math.max(0, (page - 1) * pageSize);
        return userDao.searchUsers(query, role, pageSize, offset);
    }

    public int countSearchUsers(String query, Role role) throws DatabaseException {
        return userDao.countSearchUsers(query, role);
    }

    /**
     * Transactionally creates a doctor account (users + doctors tables) (§4.6).
     */
    public Doctor createDoctor(Doctor doctor, String rawPassword, Integer adminId, String ipAddress)
            throws ValidationException, DatabaseException {
        validateAndFormatUser(doctor);

        if (userDao.findByEmail(doctor.getEmail()).isPresent()) {
            throw new ValidationException("A user with this email already exists");
        }

        String hashed = PasswordUtil.hashPassword(rawPassword);
        doctor.setPasswordHash(hashed);
        doctor.setRole(Role.DOCTOR);
        doctor.setStatus("ACTIVE");

        String insertUserSql = "INSERT INTO users (name, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?)";
        String insertDoctorSql = "INSERT INTO doctors (id, specialization, experience, consultation_fee, room_number, bio) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psDoc = conn.prepareStatement(insertDoctorSql)) {

                psUser.setString(1, doctor.getName());
                psUser.setString(2, doctor.getEmail());
                psUser.setString(3, doctor.getPasswordHash());
                psUser.setString(4, Role.DOCTOR.name());
                psUser.setString(5, "ACTIVE");
                psUser.executeUpdate();

                int docId;
                try (ResultSet keys = psUser.getGeneratedKeys()) {
                    if (keys.next()) {
                        docId = keys.getInt(1);
                        doctor.setId(docId);
                    } else {
                        throw new SQLException("Failed to obtain generated ID for doctor");
                    }
                }

                psDoc.setInt(1, docId);
                psDoc.setString(2, doctor.getSpecialization());
                psDoc.setInt(3, doctor.getExperience());
                psDoc.setBigDecimal(4, doctor.getConsultationFee() != null ? doctor.getConsultationFee() : BigDecimal.valueOf(50.00));
                psDoc.setString(5, doctor.getRoomNumber());
                psDoc.setString(6, doctor.getBio());
                psDoc.executeUpdate();

                auditDao.logAction(conn, adminId, "CREATE_DOCTOR", "Created doctor account for Dr. " + doctor.getName(), ipAddress);

                conn.commit();
                logger.info("Successfully created doctor with ID {}", docId);
                return doctor;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Rolled back doctor creation transaction", e);
                throw new DatabaseException("Failed to create doctor in transaction", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database connection error during doctor creation", e);
        }
    }

    public void updateUserStatus(int userId, String status, Integer adminId, String ipAddress) throws DatabaseException {
        userDao.updateStatus(userId, status);
        auditDao.logAction(adminId, "USER_STATUS_CHANGE", "Changed user " + userId + " status to " + status, ipAddress);
    }

    public void deleteUser(int userId, Integer adminId, String ipAddress) throws DatabaseException {
        userDao.deleteById(userId);
        auditDao.logAction(adminId, "USER_DELETE", "Deleted user ID: " + userId, ipAddress);
    }

    public Optional<Doctor> getDoctorById(int doctorId) throws DatabaseException {
        return doctorDao.findById(doctorId);
    }

    public Optional<Patient> getPatientById(int patientId) throws DatabaseException {
        return patientDao.findById(patientId);
    }

    public void updatePatientProfile(Patient patient, String ipAddress) throws DatabaseException {
        userDao.update(patient);
        patientDao.updatePatientDetails(patient);
        auditDao.logAction(patient.getId(), "PROFILE_UPDATE", "Updated personal demographic profile", ipAddress);
    }

    public void updateDoctorProfile(Doctor doctor, String ipAddress) throws DatabaseException {
        userDao.update(doctor);
        doctorDao.updateDoctorDetails(doctor);
        auditDao.logAction(doctor.getId(), "PROFILE_UPDATE", "Updated clinical doctor profile", ipAddress);
    }
}
