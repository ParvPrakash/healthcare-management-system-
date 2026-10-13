package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Notification;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationDaoImpl implements NotificationDao {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDaoImpl.class);

    private final RowMapper<Notification> notifRowMapper = rs -> {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    };

    @Override
    public Optional<Notification> findById(Integer id) throws DatabaseException {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(notifRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding notification by id {}", id, e);
            throw new DatabaseException("Database error finding notification", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Notification> findAll() throws DatabaseException {
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(notifRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all notifications", e);
            throw new DatabaseException("Database error retrieving notifications", e);
        }
        return list;
    }

    @Override
    public List<Notification> findByUserId(int userId, int limit) throws DatabaseException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(notifRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding notifications for user {}", userId, e);
            throw new DatabaseException("Database error retrieving user notifications", e);
        }
        return list;
    }

    @Override
    public int countUnreadByUserId(int userId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting unread notifications for user {}", userId, e);
            throw new DatabaseException("Database error counting unread notifications", e);
        }
        return 0;
    }

    @Override
    public void markAllRead(int userId) throws DatabaseException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error marking all notifications read for user {}", userId, e);
            throw new DatabaseException("Database error updating notifications", e);
        }
    }

    @Override
    public void markRead(int notificationId) throws DatabaseException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error marking notification read {}", notificationId, e);
            throw new DatabaseException("Database error updating notification", e);
        }
    }

    @Override
    public Notification saveTransactional(Connection conn, Notification n) throws DatabaseException {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getMessage());
            ps.setString(4, n.getType() != null ? n.getType() : "INFO");
            ps.setBoolean(5, n.isRead());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    n.setId(keys.getInt(1));
                }
            }
            return n;
        } catch (SQLException e) {
            logger.error("Error saving notification in transaction", e);
            throw new DatabaseException("Database error saving notification", e);
        }
    }

    @Override
    public Notification save(Notification entity) throws DatabaseException {
        try (Connection conn = DBConnection.getConnection()) {
            return saveTransactional(conn, entity);
        } catch (SQLException e) {
            throw new DatabaseException("Error leasing connection for notification save", e);
        }
    }

    @Override
    public boolean update(Notification entity) throws DatabaseException {
        String sql = "UPDATE notifications SET title = ?, message = ?, is_read = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getTitle());
            ps.setString(2, entity.getMessage());
            ps.setBoolean(3, entity.isRead());
            ps.setInt(4, entity.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating notification {}", entity.getId(), e);
            throw new DatabaseException("Database error updating notification", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseException {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting notification {}", id, e);
            throw new DatabaseException("Database error deleting notification", e);
        }
    }
}
