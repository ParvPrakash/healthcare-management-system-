package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDaoImpl implements AppointmentDao {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentDaoImpl.class);

    private final RowMapper<Appointment> appointmentRowMapper = rs -> {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setSlotId(rs.getInt("slot_id"));
        a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        a.setStartTime(rs.getTime("start_time").toLocalTime());
        a.setEndTime(rs.getTime("end_time").toLocalTime());
        a.setStatus(AppointmentStatus.fromString(rs.getString("status")));
        a.setReason(rs.getString("reason"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            a.setDoctorName(rs.getString("doctor_name"));
            a.setDoctorSpecialization(rs.getString("doctor_spec"));
            a.setDoctorFee(rs.getBigDecimal("consultation_fee"));
            a.setPatientName(rs.getString("patient_name"));
            a.setPatientPhone(rs.getString("patient_phone"));
            a.setPatientEmail(rs.getString("patient_email"));
            a.setPatientBloodGroup(rs.getString("patient_blood"));
        } catch (SQLException ignored) {}

        try {
            int rating = rs.getInt("feedback_rating");
            if (!rs.wasNull()) {
                a.setFeedbackRating(rating);
                a.setFeedbackComment(rs.getString("feedback_comment"));
            }
        } catch (SQLException ignored) {}

        return a;
    };

    private static final String BASE_SELECT =
            "SELECT a.*, " +
            "u_doc.name AS doctor_name, d.specialization AS doctor_spec, d.consultation_fee, " +
            "u_pat.name AS patient_name, u_pat.email AS patient_email, p.phone AS patient_phone, p.blood_group AS patient_blood, " +
            "f.rating AS feedback_rating, f.comment AS feedback_comment " +
            "FROM appointments a " +
            "JOIN users u_doc ON a.doctor_id = u_doc.id " +
            "JOIN doctors d ON a.doctor_id = d.id " +
            "JOIN users u_pat ON a.patient_id = u_pat.id " +
            "JOIN patients p ON a.patient_id = p.id " +
            "LEFT JOIN feedback f ON a.id = f.appointment_id ";

    @Override
    public Optional<Appointment> findById(Integer id) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(appointmentRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding appointment by id {}", id, e);
            throw new DatabaseException("Database error finding appointment", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findAll() throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY a.appointment_date DESC, a.start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(appointmentRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all appointments", e);
            throw new DatabaseException("Database error retrieving appointments", e);
        }
        return list;
    }

    // --- Compile-time Polymorphism (Method Overloading) ---
    @Override
    public List<Appointment> searchAppointments(int patientId, AppointmentStatus status) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE a.patient_id = ? ");
        if (status != null) {
            sql.append("AND a.status = ? ");
        }
        sql.append("ORDER BY a.appointment_date DESC, a.start_time DESC");

        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, patientId);
            if (status != null) {
                ps.setString(2, status.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(appointmentRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching appointments for patient {}", patientId, e);
            throw new DatabaseException("Database error searching patient appointments", e);
        }
        return list;
    }

    @Override
    public List<Appointment> searchAppointments(int doctorId, LocalDate date) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE a.doctor_id = ? ");
        if (date != null) {
            sql.append("AND a.appointment_date = ? ");
        }
        sql.append("ORDER BY a.appointment_date ASC, a.start_time ASC");

        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, doctorId);
            if (date != null) {
                ps.setDate(2, Date.valueOf(date));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(appointmentRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching appointments for doctor {} on {}", doctorId, date, e);
            throw new DatabaseException("Database error searching doctor appointments", e);
        }
        return list;
    }

    @Override
    public List<Appointment> searchAppointments(String query, AppointmentStatus status, int limit, int offset) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append("AND a.status = ? ");
            params.add(status.name());
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(u_doc.name) LIKE ? OR LOWER(u_pat.name) LIKE ? OR LOWER(a.reason) LIKE ?) ");
            String wildcard = "%" + query.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }
        sql.append("ORDER BY a.appointment_date DESC, a.start_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(appointmentRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching appointments with query {}", query, e);
            throw new DatabaseException("Database error searching appointments", e);
        }
        return list;
    }

    @Override
    public List<Appointment> findUpcomingByDoctor(int doctorId, int limit) throws DatabaseException {
        String sql = BASE_SELECT +
                "WHERE a.doctor_id = ? AND a.appointment_date >= CURRENT_DATE AND a.status IN ('CONFIRMED', 'PENDING', 'RESCHEDULED') " +
                "ORDER BY a.appointment_date ASC, a.start_time ASC LIMIT ?";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(appointmentRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving upcoming appointments for doctor {}", doctorId, e);
            throw new DatabaseException("Database error retrieving doctor upcoming appointments", e);
        }
        return list;
    }

    @Override
    public List<Appointment> findPastByDoctor(int doctorId, int limit) throws DatabaseException {
        String sql = BASE_SELECT +
                "WHERE a.doctor_id = ? AND (a.appointment_date < CURRENT_DATE OR a.status IN ('COMPLETED', 'CANCELLED')) " +
                "ORDER BY a.appointment_date DESC, a.start_time DESC LIMIT ?";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(appointmentRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving past appointments for doctor {}", doctorId, e);
            throw new DatabaseException("Database error retrieving doctor past appointments", e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(appointmentRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving appointments for patient {}", patientId, e);
            throw new DatabaseException("Database error retrieving patient appointments", e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByDoctorId(int doctorId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.doctor_id = ? ORDER BY a.appointment_date DESC, a.start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(appointmentRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving appointments for doctor {}", doctorId, e);
            throw new DatabaseException("Database error retrieving doctor appointments", e);
        }
        return list;
    }

    @Override
    public List<Appointment> findTodayAppointments() throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.appointment_date = CURRENT_DATE ORDER BY a.start_time ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(appointmentRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving today's appointments", e);
            throw new DatabaseException("Database error retrieving today's appointments", e);
        }
        return list;
    }

    @Override
    public int countTotal() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM appointments";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting appointments", e);
            throw new DatabaseException("Database error counting appointments", e);
        }
        return 0;
    }

    @Override
    public int countToday() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting today's appointments", e);
            throw new DatabaseException("Database error counting today's appointments", e);
        }
        return 0;
    }

    @Override
    public int countByStatus(AppointmentStatus status) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting appointments by status {}", status, e);
            throw new DatabaseException("Database error counting appointments by status", e);
        }
        return 0;
    }

    @Override
    public Appointment saveTransactional(Connection conn, Appointment a) throws DatabaseException {
        String sql = "INSERT INTO appointments (doctor_id, patient_id, slot_id, appointment_date, start_time, end_time, status, reason, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getDoctorId());
            ps.setInt(2, a.getPatientId());
            ps.setInt(3, a.getSlotId());
            ps.setDate(4, Date.valueOf(a.getAppointmentDate()));
            ps.setTime(5, Time.valueOf(a.getStartTime()));
            ps.setTime(6, Time.valueOf(a.getEndTime()));
            ps.setString(7, a.getStatus() != null ? a.getStatus().name() : "CONFIRMED");
            ps.setString(8, a.getReason());
            ps.setString(9, a.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getInt(1));
                }
            }
            return a;
        } catch (SQLException e) {
            logger.error("Error saving appointment in transaction", e);
            throw new DatabaseException("Database error saving transactional appointment", e);
        }
    }

    @Override
    public boolean updateStatusTransactional(Connection conn, int appointmentId, AppointmentStatus status) throws DatabaseException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for appointment {}", appointmentId, e);
            throw new DatabaseException("Database error updating appointment status", e);
        }
    }

    @Override
    public boolean rescheduleTransactional(Connection conn, int appointmentId, int newSlotId, LocalDate newDate, LocalTime newStart, LocalTime newEnd) throws DatabaseException {
        String sql = "UPDATE appointments SET slot_id = ?, appointment_date = ?, start_time = ?, end_time = ?, status = 'RESCHEDULED' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newSlotId);
            ps.setDate(2, Date.valueOf(newDate));
            ps.setTime(3, Time.valueOf(newStart));
            ps.setTime(4, Time.valueOf(newEnd));
            ps.setInt(5, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error rescheduling appointment {}", appointmentId, e);
            throw new DatabaseException("Database error rescheduling appointment", e);
        }
    }

    @Override
    public Appointment save(Appointment entity) throws DatabaseException {
        try (Connection conn = DBConnection.getConnection()) {
            return saveTransactional(conn, entity);
        } catch (SQLException e) {
            throw new DatabaseException("Error acquiring connection to save appointment", e);
        }
    }

    @Override
    public boolean update(Appointment entity) throws DatabaseException {
        String sql = "UPDATE appointments SET status = ?, reason = ?, notes = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getStatus().name());
            ps.setString(2, entity.getReason());
            ps.setString(3, entity.getNotes());
            ps.setInt(4, entity.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating appointment {}", entity.getId(), e);
            throw new DatabaseException("Database error updating appointment", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseException {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting appointment {}", id, e);
            throw new DatabaseException("Database error deleting appointment", e);
        }
    }
}
