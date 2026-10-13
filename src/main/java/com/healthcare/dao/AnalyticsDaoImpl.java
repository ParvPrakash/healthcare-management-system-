package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsDaoImpl implements AnalyticsDao {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDaoImpl.class);

    @Override
    public Map<String, Integer> getAppointmentsPerDay(int days) throws DatabaseException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(appointment_date, '%Y-%m-%d') AS dt, COUNT(*) AS cnt " +
                "FROM appointments " +
                "WHERE appointment_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) " +
                "GROUP BY dt ORDER BY dt ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("dt"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving appointments per day", e);
            throw new DatabaseException("Database error generating appointments per day trend", e);
        }
        return data;
    }

    @Override
    public Map<String, Integer> getAppointmentsByStatus() throws DatabaseException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM appointments GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving appointments by status", e);
            throw new DatabaseException("Database error generating status distribution", e);
        }
        return data;
    }

    @Override
    public Map<String, Integer> getTopDoctors(int limit) throws DatabaseException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT u.name, COUNT(a.id) AS cnt " +
                "FROM appointments a " +
                "JOIN users u ON a.doctor_id = u.id " +
                "WHERE a.status = 'COMPLETED' " +
                "GROUP BY u.id, u.name ORDER BY cnt DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("name"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving top doctors", e);
            throw new DatabaseException("Database error calculating top doctors", e);
        }
        return data;
    }

    @Override
    public Map<String, Integer> getNewUsersTrend(int days) throws DatabaseException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS dt, COUNT(*) AS cnt " +
                "FROM users " +
                "WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) " +
                "GROUP BY dt ORDER BY dt ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("dt"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving new users trend", e);
            throw new DatabaseException("Database error calculating new user registrations", e);
        }
        return data;
    }

    @Override
    public Map<String, Integer> getBusiestHours() throws DatabaseException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT HOUR(start_time) AS hr, COUNT(*) AS cnt " +
                "FROM appointments GROUP BY hr ORDER BY hr ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int hr = rs.getInt("hr");
                String label = String.format("%02d:00", hr);
                data.put(label, rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving busiest hours", e);
            throw new DatabaseException("Database error calculating busiest hours", e);
        }
        return data;
    }

    @Override
    public Map<String, Object> getSystemOverviewStats() throws DatabaseException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM users) AS total_users, " +
                "(SELECT COUNT(*) FROM doctors) AS total_doctors, " +
                "(SELECT COUNT(*) FROM patients) AS total_patients, " +
                "(SELECT COUNT(*) FROM appointments) AS total_appointments, " +
                "(SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE) AS appointments_today, " +
                "(SELECT COUNT(*) FROM appointments WHERE status = 'PENDING') AS pending_appointments, " +
                "(SELECT COALESCE(AVG(rating), 0.0) FROM feedback) AS avg_clinic_rating, " +
                "(SELECT COALESCE(SUM(d.consultation_fee), 0.00) FROM appointments a JOIN doctors d ON a.doctor_id = d.id WHERE a.status = 'COMPLETED') AS total_revenue";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("totalUsers", rs.getInt("total_users"));
                stats.put("totalDoctors", rs.getInt("total_doctors"));
                stats.put("totalPatients", rs.getInt("total_patients"));
                stats.put("totalAppointments", rs.getInt("total_appointments"));
                stats.put("appointmentsToday", rs.getInt("appointments_today"));
                stats.put("pendingAppointments", rs.getInt("pending_appointments"));
                stats.put("avgClinicRating", Math.round(rs.getDouble("avg_clinic_rating") * 10.0) / 10.0);
                stats.put("totalRevenue", rs.getBigDecimal("total_revenue"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving system overview stats", e);
            throw new DatabaseException("Database error calculating system KPIs", e);
        }
        return stats;
    }
}
