package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.MedicalRecord;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicalRecordDaoImpl implements MedicalRecordDao {
    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordDaoImpl.class);

    private final RowMapper<MedicalRecord> recordRowMapper = rs -> {
        MedicalRecord mr = new MedicalRecord();
        mr.setId(rs.getInt("id"));
        mr.setAppointmentId(rs.getObject("appointment_id") != null ? rs.getInt("appointment_id") : null);
        mr.setPatientId(rs.getInt("patient_id"));
        mr.setDoctorId(rs.getInt("doctor_id"));
        mr.setDiagnosis(rs.getString("diagnosis"));
        mr.setPrescription(rs.getString("prescription"));
        mr.setVitalsBloodPressure(rs.getString("vitals_blood_pressure"));
        mr.setVitalsHeartRate(rs.getString("vitals_heart_rate"));
        mr.setVitalsTemperature(rs.getString("vitals_temperature"));
        mr.setNotes(rs.getString("notes"));
        mr.setRecordDate(rs.getDate("record_date").toLocalDate());
        mr.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            mr.setDoctorName(rs.getString("doctor_name"));
            mr.setDoctorSpecialization(rs.getString("doctor_spec"));
            mr.setPatientName(rs.getString("patient_name"));
        } catch (SQLException ignored) {}

        return mr;
    };

    private static final String BASE_SELECT =
            "SELECT mr.*, u_doc.name AS doctor_name, d.specialization AS doctor_spec, u_pat.name AS patient_name " +
            "FROM medical_records mr " +
            "JOIN users u_doc ON mr.doctor_id = u_doc.id " +
            "JOIN doctors d ON mr.doctor_id = d.id " +
            "JOIN users u_pat ON mr.patient_id = u_pat.id ";

    @Override
    public Optional<MedicalRecord> findById(Integer id) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE mr.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(recordRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding medical record by id {}", id, e);
            throw new DatabaseException("Database error finding medical record", e);
        }
        return Optional.empty();
    }

    @Override
    public List<MedicalRecord> findAll() throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY mr.record_date DESC, mr.id DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(recordRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all medical records", e);
            throw new DatabaseException("Database error retrieving medical records", e);
        }
        return list;
    }

    @Override
    public List<MedicalRecord> findByPatientId(int patientId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE mr.patient_id = ? ORDER BY mr.record_date DESC, mr.id DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(recordRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving medical records for patient {}", patientId, e);
            throw new DatabaseException("Database error retrieving patient medical records", e);
        }
        return list;
    }

    @Override
    public List<MedicalRecord> findByDoctorId(int doctorId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE mr.doctor_id = ? ORDER BY mr.record_date DESC, mr.id DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(recordRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving medical records for doctor {}", doctorId, e);
            throw new DatabaseException("Database error retrieving doctor medical records", e);
        }
        return list;
    }

    @Override
    public Optional<MedicalRecord> findByAppointmentId(int appointmentId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE mr.appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(recordRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding medical record for appointment {}", appointmentId, e);
            throw new DatabaseException("Database error finding appointment medical record", e);
        }
        return Optional.empty();
    }

    @Override
    public MedicalRecord save(MedicalRecord entity) throws DatabaseException {
        String sql = "INSERT INTO medical_records (appointment_id, patient_id, doctor_id, diagnosis, prescription, vitals_blood_pressure, vitals_heart_rate, vitals_temperature, notes, record_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, entity.getAppointmentId(), Types.INTEGER);
            ps.setInt(2, entity.getPatientId());
            ps.setInt(3, entity.getDoctorId());
            ps.setString(4, entity.getDiagnosis());
            ps.setString(5, entity.getPrescription());
            ps.setString(6, entity.getVitalsBloodPressure());
            ps.setString(7, entity.getVitalsHeartRate());
            ps.setString(8, entity.getVitalsTemperature());
            ps.setString(9, entity.getNotes());
            ps.setDate(10, Date.valueOf(entity.getRecordDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            logger.error("Error saving medical record", e);
            throw new DatabaseException("Database error saving medical record", e);
        }
    }

    @Override
    public boolean update(MedicalRecord entity) throws DatabaseException {
        String sql = "UPDATE medical_records SET diagnosis = ?, prescription = ?, vitals_blood_pressure = ?, vitals_heart_rate = ?, vitals_temperature = ?, notes = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getDiagnosis());
            ps.setString(2, entity.getPrescription());
            ps.setString(3, entity.getVitalsBloodPressure());
            ps.setString(4, entity.getVitalsHeartRate());
            ps.setString(5, entity.getVitalsTemperature());
            ps.setString(6, entity.getNotes());
            ps.setInt(7, entity.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating medical record {}", entity.getId(), e);
            throw new DatabaseException("Database error updating medical record", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseException {
        String sql = "DELETE FROM medical_records WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting medical record {}", id, e);
            throw new DatabaseException("Database error deleting medical record", e);
        }
    }
}
