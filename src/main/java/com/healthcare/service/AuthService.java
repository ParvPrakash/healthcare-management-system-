package com.healthcare.service;

import com.healthcare.dao.AuditDao;
import com.healthcare.dao.AuditDaoImpl;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.PatientDaoImpl;
import com.healthcare.dao.UserDao;
import com.healthcare.dao.UserDaoImpl;
import com.healthcare.exception.AuthenticationException;
import com.healthcare.exception.DatabaseException;
import com.healthcare.exception.ValidationException;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.util.DBConnection;
import com.healthcare.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service orchestrating authentication, security policies, and user registration.
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final PatientDao patientDao;
    private final AuditDao auditDao;

    public AuthService() {
        this.userDao = new UserDaoImpl();
        this.patientDao = new PatientDaoImpl();
        this.auditDao = new AuditDaoImpl();
    }

    public AuthService(UserDao userDao, PatientDao patientDao, AuditDao auditDao) {
        this.userDao = userDao;
        this.patientDao = patientDao;
        this.auditDao = auditDao;
    }

    /**
     * Authenticates a user with email and password.
     * Enforces account lockout rules (5 attempts = 15 min lock).
     */
    public User login(String email, String password, String ipAddress) throws AuthenticationException, DatabaseException {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Email and password cannot be empty");
        }

        email = email.trim();
        Optional<User> userOpt = userDao.findByEmail(email);

        if (userOpt.isEmpty()) {
            auditDao.logAction(null, "LOGIN_FAILED", "Email not found: " + email, ipAddress);
            throw new AuthenticationException("Invalid email or password");
        }

        User user = userOpt.get();

        // Check if account is locked
        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            Timestamp lockedUntil = user.getLockedUntil();
            if (lockedUntil != null && lockedUntil.after(new Timestamp(System.currentTimeMillis()))) {
                long minutesLeft = (lockedUntil.getTime() - System.currentTimeMillis()) / (60 * 1000);
                throw new AuthenticationException("Account is locked due to multiple failed attempts. Try again in " + (minutesLeft + 1) + " minutes.");
            } else {
                // Lock expired, reset
                userDao.resetFailedLogins(email);
                user.setStatus("ACTIVE");
            }
        }

        if (!user.isActive()) {
            throw new AuthenticationException("Your account is currently disabled. Please contact administrator.");
        }

        // Validate password
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            userDao.recordFailedLogin(email);
            auditDao.logAction(user.getId(), "LOGIN_FAILED", "Incorrect password attempt for: " + email, ipAddress);
            throw new AuthenticationException("Invalid email or password");
        }

        // Login successful
        userDao.resetFailedLogins(email);
        auditDao.logAction(user.getId(), "LOGIN_SUCCESS", "User logged in successfully", ipAddress);

        return user;
    }

    /**
     * Registers a new patient.
     * Uses ACID transaction across users and patients tables (§4.6).
     */
    public Patient registerPatient(Patient patient, String rawPassword, String ipAddress)
            throws ValidationException, DatabaseException {
        validateRegistrationInput(patient, rawPassword);

        // Check if email already exists
        if (userDao.findByEmail(patient.getEmail()).isPresent()) {
            Map<String, String> errors = new HashMap<>();
            errors.put("email", "An account with this email address already exists");
            throw new ValidationException("Email address already registered", errors);
        }

        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        patient.setPasswordHash(hashedPassword);
        patient.setRole(Role.PATIENT);
        patient.setStatus("ACTIVE");

        // Section 4.6: Transaction management: insert into users + patients
        String insertUserSql = "INSERT INTO users (name, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?)";
        String insertPatientSql = "INSERT INTO patients (id, age, gender, phone, blood_group, address, emergency_contact) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psPatient = conn.prepareStatement(insertPatientSql)) {

                // 1. Insert user
                psUser.setString(1, patient.getName());
                psUser.setString(2, patient.getEmail());
                psUser.setString(3, patient.getPasswordHash());
                psUser.setString(4, Role.PATIENT.name());
                psUser.setString(5, "ACTIVE");
                psUser.executeUpdate();

                int userId;
                try (ResultSet keys = psUser.getGeneratedKeys()) {
                    if (keys.next()) {
                        userId = keys.getInt(1);
                        patient.setId(userId);
                    } else {
                        throw new SQLException("Failed to obtain generated user ID");
                    }
                }

                // 2. Insert patient profile
                psPatient.setInt(1, userId);
                psPatient.setObject(2, patient.getAge());
                psPatient.setString(3, patient.getGender());
                psPatient.setString(4, patient.getPhone());
                psPatient.setString(5, patient.getBloodGroup());
                psPatient.setString(6, patient.getAddress());
                psPatient.setString(7, patient.getEmergencyContact());
                psPatient.executeUpdate();

                // 3. Log audit in transaction
                auditDao.logAction(conn, userId, "USER_REGISTERED", "Self-registered patient: " + patient.getEmail(), ipAddress);

                conn.commit();
                logger.info("Successfully registered new patient ID: {}", userId);
                return patient;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction rolled back during patient registration", e);
                throw new DatabaseException("Failed to register patient due to transaction rollback", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during registration", e);
            throw new DatabaseException("Database connection failure", e);
        }
    }

    /**
     * Updates user password after validating current password.
     */
    public void changePassword(int userId, String oldPassword, String newPassword, String ipAddress)
            throws ValidationException, AuthenticationException, DatabaseException {
        if (newPassword == null || newPassword.length() < 6) {
            Map<String, String> err = new HashMap<>();
            err.put("newPassword", "New password must be at least 6 characters long");
            throw new ValidationException("Invalid new password", err);
        }

        User user = userDao.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User account not found"));

        if (!PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password does not match");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        userDao.updatePassword(userId, newHash);
        auditDao.logAction(userId, "PASSWORD_CHANGED", "User successfully changed password", ipAddress);
    }

    private void validateRegistrationInput(Patient patient, String rawPassword) throws ValidationException {
        Map<String, String> errors = new HashMap<>();
        if (patient.getName() == null || patient.getName().trim().length() < 2) {
            errors.put("name", "Name must be at least 2 characters");
        }
        if (patient.getEmail() == null || !patient.getEmail().contains("@") || !patient.getEmail().contains(".")) {
            errors.put("email", "A valid email address is required");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            errors.put("password", "Password must be at least 6 characters");
        }
        if (patient.getPhone() == null || patient.getPhone().trim().length() < 7) {
            errors.put("phone", "Valid contact phone number is required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed for registration input", errors);
        }
    }
}
