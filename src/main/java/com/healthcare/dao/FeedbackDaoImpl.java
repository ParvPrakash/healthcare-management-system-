package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Feedback;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class FeedbackDaoImpl implements FeedbackDao {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackDaoImpl.class);

    private final RowMapper<Feedback> feedbackRowMapper = rs -> {
        Feedback f = new Feedback();
        f.setId(rs.getInt("id"));
        f.setAppointmentId(rs.getInt("appointment_id"));
        f.setDoctorId(rs.getInt("doctor_id"));
        f.setPatientId(rs.getInt("patient_id"));
        f.setRating(rs.getInt("rating"));
        f.setComment(rs.getString("comment"));
        f.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            f.setPatientName(rs.getString("patient_name"));
            f.setDoctorName(rs.getString("doctor_name"));
        } catch (SQLException ignored) {}

        return f;
    };

    private static final String BASE_SELECT =
            "SELECT f.*, u_pat.name AS patient_name, u_doc.name AS doctor_name " +
            "FROM feedback f " +
            "JOIN users u_pat ON f.patient_id = u_pat.id " +
            "JOIN users u_doc ON f.doctor_id = u_doc.id ";

    @Override
    public Optional<Feedback> findById(Integer id) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE f.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(feedbackRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding feedback by id {}", id, e);
            throw new DatabaseException("Database error finding feedback", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Feedback> findAll() throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY f.created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(feedbackRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all feedback", e);
            throw new DatabaseException("Database error retrieving feedback", e);
        }
        return list;
    }

    @Override
    public List<Feedback> findByDoctorId(int doctorId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE f.doctor_id = ? ORDER BY f.created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(feedbackRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving feedback for doctor {}", doctorId, e);
            throw new DatabaseException("Database error retrieving doctor reviews", e);
        }
        return list;
    }

    @Override
    public Map<Integer, Integer> getRatingDistribution(int doctorId) throws DatabaseException {
        // Map / HashMap / TreeMap usage
        Map<Integer, Integer> dist = new java.util.LinkedHashMap<>() {
            @Override
            public Integer get(Object key) {
                if (key instanceof Number n) {
                    Integer val = super.get(n.intValue());
                    return val != null ? val : 0;
                }
                return super.get(key);
            }
        };
        for (int i = 5; i >= 1; i--) {
            dist.put(i, 0);
        }

        String sql = "SELECT rating, COUNT(*) AS cnt FROM feedback WHERE doctor_id = ? GROUP BY rating";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dist.put(rs.getInt("rating"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting rating distribution for doctor {}", doctorId, e);
            throw new DatabaseException("Database error calculating rating distribution", e);
        }
        return dist;
    }

    @Override
    public double getAverageRating(int doctorId) throws DatabaseException {
        String sql = "SELECT COALESCE(AVG(rating), 0.0) FROM feedback WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting average rating for doctor {}", doctorId, e);
            throw new DatabaseException("Database error calculating average rating", e);
        }
        return 0.0;
    }

    @Override
    public Optional<Feedback> findByAppointmentId(int appointmentId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE f.appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(feedbackRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding feedback for appointment {}", appointmentId, e);
            throw new DatabaseException("Database error retrieving appointment feedback", e);
        }
        return Optional.empty();
    }

    @Override
    public Feedback save(Feedback entity) throws DatabaseException {
        String sql = "INSERT INTO feedback (appointment_id, doctor_id, patient_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entity.getAppointmentId());
            ps.setInt(2, entity.getDoctorId());
            ps.setInt(3, entity.getPatientId());
            ps.setInt(4, entity.getRating());
            ps.setString(5, entity.getComment());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            logger.error("Error saving feedback", e);
            throw new DatabaseException("Database error saving feedback review", e);
        }
    }

    @Override
    public boolean update(Feedback entity) throws DatabaseException {
        String sql = "UPDATE feedback SET rating = ?, comment = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entity.getRating());
            ps.setString(2, entity.getComment());
            ps.setInt(3, entity.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating feedback {}", entity.getId(), e);
            throw new DatabaseException("Database error updating review", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseException {
        String sql = "DELETE FROM feedback WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting feedback {}", id, e);
            throw new DatabaseException("Database error deleting review", e);
        }
    }
}
