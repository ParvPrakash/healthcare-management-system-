package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.AuditLog;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuditDaoImpl implements AuditDao {
    private static final Logger logger = LoggerFactory.getLogger(AuditDaoImpl.class);

    private final RowMapper<AuditLog> auditRowMapper = rs -> {
        AuditLog al = new AuditLog();
        al.setId(rs.getInt("id"));
        al.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        al.setAction(rs.getString("action"));
        al.setDetails(rs.getString("details"));
        al.setIpAddress(rs.getString("ip_address"));
        al.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            al.setUserEmail(rs.getString("user_email"));
        } catch (SQLException ignored) {}
        return al;
    };

    private static final String BASE_SELECT =
            "SELECT a.*, u.email AS user_email " +
            "FROM audit_logs a " +
            "LEFT JOIN users u ON a.user_id = u.id ";

    @Override
    public Optional<AuditLog> findById(Integer id) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(auditRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding audit log by id {}", id, e);
            throw new DatabaseException("Database error finding audit log", e);
        }
        return Optional.empty();
    }

    @Override
    public List<AuditLog> findAll() throws DatabaseException {
        return findRecent(100);
    }

    @Override
    public List<AuditLog> findRecent(int limit) throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY a.created_at DESC, a.id DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(auditRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recent audit logs", e);
            throw new DatabaseException("Database error retrieving recent audit logs", e);
        }
        return list;
    }

    @Override
    public List<AuditLog> searchLogs(String query, int limit, int offset) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(a.action) LIKE ? OR LOWER(a.details) LIKE ? OR LOWER(u.email) LIKE ?) ");
            String wildcard = "%" + query.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }
        sql.append("ORDER BY a.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(auditRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching audit logs", e);
            throw new DatabaseException("Database error searching audit logs", e);
        }
        return list;
    }

    @Override
    public int countSearchLogs(String query) throws DatabaseException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs a LEFT JOIN users u ON a.user_id = u.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(a.action) LIKE ? OR LOWER(a.details) LIKE ? OR LOWER(u.email) LIKE ?) ");
            String wildcard = "%" + query.trim().toLowerCase() + "%";
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
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting audit logs", e);
            throw new DatabaseException("Database error counting audit logs", e);
        }
        return 0;
    }

    @Override
    public void logAction(Integer userId, String action, String details, String ipAddress) throws DatabaseException {
        String sql = "INSERT INTO audit_logs (user_id, action, details, ip_address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId, Types.INTEGER);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setString(4, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error recording audit log", e);
            throw new DatabaseException("Database error logging audit event", e);
        }
    }

    @Override
    public void logAction(Connection conn, Integer userId, String action, String details, String ipAddress) throws DatabaseException {
        String sql = "INSERT INTO audit_logs (user_id, action, details, ip_address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId, Types.INTEGER);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setString(4, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error recording audit log in transaction", e);
            throw new DatabaseException("Database error logging audit event in transaction", e);
        }
    }

    @Override
    public AuditLog save(AuditLog entity) throws DatabaseException {
        logAction(entity.getUserId(), entity.getAction(), entity.getDetails(), entity.getIpAddress());
        return entity;
    }

    @Override
    public boolean update(AuditLog entity) throws DatabaseException {
        return false; // Immutable audit log
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseException {
        return false; // Audit logs are immutable for compliance
    }
}
