package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Doctor;
import com.healthcare.model.Role;
import com.healthcare.util.DBConnection;
import com.healthcare.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorDaoImpl implements DoctorDao {
    private static final Logger logger = LoggerFactory.getLogger(DoctorDaoImpl.class);

    private final RowMapper<Doctor> doctorRowMapper = rs -> {
        Doctor doc = new Doctor();
        doc.setId(rs.getInt("id"));
        doc.setName(rs.getString("name"));
        doc.setEmail(rs.getString("email"));
        doc.setPasswordHash(rs.getString("password_hash"));
        doc.setRole(Role.DOCTOR);
        doc.setStatus(rs.getString("status"));
        doc.setCreatedAt(rs.getTimestamp("created_at"));
        doc.setSpecialization(rs.getString("specialization"));
        doc.setExperience(rs.getInt("experience"));
        doc.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        doc.setRoomNumber(rs.getString("room_number"));
        doc.setBio(rs.getString("bio"));

        try {
            doc.setAverageRating(rs.getDouble("avg_rating"));
            doc.setTotalReviews(rs.getInt("total_reviews"));
        } catch (SQLException ignored) {
            // In case columns are not present in query projection
        }
        return doc;
    };

    private static final String BASE_DOCTOR_SELECT =
            "SELECT u.*, d.specialization, d.experience, d.consultation_fee, d.room_number, d.bio, " +
            "COALESCE(AVG(f.rating), 0.0) AS avg_rating, COUNT(f.id) AS total_reviews " +
            "FROM users u " +
            "JOIN doctors d ON u.id = d.id " +
            "LEFT JOIN feedback f ON d.id = f.doctor_id ";

    @Override
    public Optional<Doctor> findById(Integer id) throws DatabaseException {
        String sql = BASE_DOCTOR_SELECT + "WHERE u.id = ? GROUP BY u.id, d.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(doctorRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding doctor by id {}", id, e);
            throw new DatabaseException("Database error finding doctor by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Doctor> findAll() throws DatabaseException {
        String sql = BASE_DOCTOR_SELECT + "GROUP BY u.id, d.id ORDER BY u.name ASC";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(doctorRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all doctors", e);
            throw new DatabaseException("Database error retrieving doctors", e);
        }
        return list;
    }

    @Override
    public List<Doctor> findBySpecialization(String specialization) throws DatabaseException {
        String sql = BASE_DOCTOR_SELECT + "WHERE LOWER(d.specialization) = LOWER(?) GROUP BY u.id, d.id ORDER BY u.name ASC";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, specialization);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(doctorRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding doctors by specialization {}", specialization, e);
            throw new DatabaseException("Database error retrieving doctors by specialization", e);
        }
        return list;
    }

    @Override
    public List<String> findAllSpecializations() throws DatabaseException {
        String sql = "SELECT DISTINCT specialization FROM doctors ORDER BY specialization ASC";
        List<String> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("specialization"));
            }
        } catch (SQLException e) {
            logger.error("Error finding all specializations", e);
            throw new DatabaseException("Database error retrieving specializations", e);
        }
        return list;
    }

    @Override
    public List<Doctor> searchDoctors(String keyword, String specialization, Double minRating) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_DOCTOR_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (specialization != null && !specialization.trim().isEmpty() && !"ALL".equalsIgnoreCase(specialization.trim())) {
            sql.append("AND LOWER(d.specialization) = LOWER(?) ");
            params.add(specialization.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(u.name) LIKE ? OR LOWER(d.specialization) LIKE ? OR LOWER(d.bio) LIKE ?) ");
            String wildcard = "%" + keyword.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        sql.append("GROUP BY u.id, d.id ");

        if (minRating != null && minRating > 0) {
            sql.append("HAVING avg_rating >= ? ");
            params.add(minRating);
        }

        sql.append("ORDER BY avg_rating DESC, u.name ASC");

        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(doctorRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching doctors", e);
            throw new DatabaseException("Database error searching doctors", e);
        }
        return list;
    }

    @Override
    public void saveDoctorDetails(Doctor doctor) throws DatabaseException {
        String sql = "INSERT INTO doctors (id, specialization, experience, consultation_fee, room_number, bio) " +
                "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "specialization=VALUES(specialization), experience=VALUES(experience), " +
                "consultation_fee=VALUES(consultation_fee), room_number=VALUES(room_number), bio=VALUES(bio)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctor.getId());
            ps.setString(2, doctor.getSpecialization());
            ps.setInt(3, doctor.getExperience());
            ps.setBigDecimal(4, doctor.getConsultationFee() != null ? doctor.getConsultationFee() : BigDecimal.valueOf(50.00));
            ps.setString(5, doctor.getRoomNumber());
            ps.setString(6, doctor.getBio());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving doctor details for id {}", doctor.getId(), e);
            throw new DatabaseException("Database error saving doctor clinical details", e);
        }
    }

    @Override
    public void updateDoctorDetails(Doctor doctor) throws DatabaseException {
        String sql = "UPDATE doctors SET specialization = ?, experience = ?, consultation_fee = ?, room_number = ?, bio = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.getSpecialization());
            ps.setInt(2, doctor.getExperience());
            ps.setBigDecimal(3, doctor.getConsultationFee());
            ps.setString(4, doctor.getRoomNumber());
            ps.setString(5, doctor.getBio());
            ps.setInt(6, doctor.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating doctor details for id {}", doctor.getId(), e);
            throw new DatabaseException("Database error updating doctor clinical details", e);
        }
    }

    @Override
    public Pair<Double, Integer> getDoctorRatingStats(int doctorId) throws DatabaseException {
        String sql = "SELECT COALESCE(AVG(rating), 0.0) AS avg_rating, COUNT(*) AS count FROM feedback WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Pair.of(rs.getDouble("avg_rating"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching rating stats for doctor {}", doctorId, e);
            throw new DatabaseException("Database error fetching rating stats", e);
        }
        return Pair.of(0.0, 0);
    }

    @Override
    public Doctor save(Doctor entity) throws DatabaseException {
        saveDoctorDetails(entity);
        return entity;
    }

    @Override
    public boolean update(Doctor entity) throws DatabaseException {
        updateDoctorDetails(entity);
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
            logger.error("Error deleting doctor id {}", id, e);
            throw new DatabaseException("Database error deleting doctor", e);
        }
    }
}
