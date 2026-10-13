package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDaoImpl implements PatientDao {
    private static final Logger logger = LoggerFactory.getLogger(PatientDaoImpl.class);

    private final RowMapper<Patient> patientRowMapper = rs -> {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setEmail(rs.getString("email"));
        p.setPasswordHash(rs.getString("password_hash"));
        p.setRole(Role.PATIENT);
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setAge(rs.getObject("age") != null ? rs.getInt("age") : null);
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        return p;
    };

    private static final String BASE_PATIENT_SELECT =
            "SELECT u.*, p.age, p.gender, p.phone, p.blood_group, p.address, p.emergency_contact " +
            "FROM users u " +
            "JOIN patients p ON u.id = p.id ";

    @Override
    public Optional<Patient> findById(Integer id) throws DatabaseException {
        String sql = BASE_PATIENT_SELECT + "WHERE u.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(patientRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding patient by id {}", id, e);
            throw new DatabaseException("Database error finding patient by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Patient> findAll() throws DatabaseException {
        String sql = BASE_PATIENT_SELECT + "ORDER BY u.name ASC";
        List<Patient> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(patientRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all patients", e);
            throw new DatabaseException("Database error retrieving patients", e);
        }
        return list;
    }

    @Override
    public List<Patient> searchPatients(String keyword, int limit, int offset) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_PATIENT_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(p.phone) LIKE ?) ");
            String wildcard = "%" + keyword.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }
        sql.append("ORDER BY u.name ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Patient> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(patientRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching patients", e);
            throw new DatabaseException("Database error searching patients", e);
        }
        return list;
    }

    @Override
    public int countSearchPatients(String keyword) throws DatabaseException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u JOIN patients p ON u.id = p.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(p.phone) LIKE ?) ");
            String wildcard = "%" + keyword.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting search patients", e);
            throw new DatabaseException("Database error counting search patients", e);
        }
        return 0;
    }

    @Override
    public void savePatientDetails(Patient patient) throws DatabaseException {
        String sql = "INSERT INTO patients (id, age, gender, phone, blood_group, address, emergency_contact) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "age=VALUES(age), gender=VALUES(gender), phone=VALUES(phone), " +
                "blood_group=VALUES(blood_group), address=VALUES(address), emergency_contact=VALUES(emergency_contact)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patient.getId());
            ps.setObject(2, patient.getAge(), Types.INTEGER);
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getBloodGroup());
            ps.setString(6, patient.getAddress());
            ps.setString(7, patient.getEmergencyContact());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving patient details for id {}", patient.getId(), e);
            throw new DatabaseException("Database error saving patient demographics", e);
        }
    }

    @Override
    public void updatePatientDetails(Patient patient) throws DatabaseException {
        String sql = "UPDATE patients SET age = ?, gender = ?, phone = ?, blood_group = ?, address = ?, emergency_contact = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, patient.getAge(), Types.INTEGER);
            ps.setString(2, patient.getGender());
            ps.setString(3, patient.getPhone());
            ps.setString(4, patient.getBloodGroup());
            ps.setString(5, patient.getAddress());
            ps.setString(6, patient.getEmergencyContact());
            ps.setInt(7, patient.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating patient details for id {}", patient.getId(), e);
            throw new DatabaseException("Database error updating patient demographics", e);
        }
    }

    @Override
    public Patient save(Patient entity) throws DatabaseException {
        savePatientDetails(entity);
        return entity;
    }

    @Override
    public boolean update(Patient entity) throws DatabaseException {
        updatePatientDetails(entity);
        return true;
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting patient id {}", id, e);
            throw new DatabaseException("Database error deleting patient", e);
        }
    }
}
