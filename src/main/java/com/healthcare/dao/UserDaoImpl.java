package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Admin;
import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    // Functional RowMapper instantiating the right polymorphic subclass based on Role column
    private final RowMapper<User> userRowMapper = rs -> {
        String roleStr = rs.getString("role");
        Role role = Role.fromString(roleStr);
        User user;
        if (role == Role.ADMIN) {
            user = new Admin();
        } else if (role == Role.DOCTOR) {
            user = new Doctor();
        } else {
            user = new Patient();
        }
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(role);
        user.setStatus(rs.getString("status"));
        user.setFailedAttempts(rs.getInt("failed_attempts"));
        user.setLockedUntil(rs.getTimestamp("locked_until"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    };

    @Override
    public Optional<User> findById(Integer id) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(userRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new DatabaseException("Database error finding user by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(userRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new DatabaseException("Database error finding user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        String sql = "SELECT * FROM users ORDER BY name ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(userRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all users", e);
            throw new DatabaseException("Database error retrieving users", e);
        }
        return list;
    }

    @Override
    public List<User> findByRole(Role role, int limit, int offset) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY name ASC LIMIT ? OFFSET ?";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(userRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving users by role {}", role, e);
            throw new DatabaseException("Database error retrieving users by role", e);
        }
        return list;
    }

    @Override
    public int countByRole(Role role) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting users by role {}", role, e);
            throw new DatabaseException("Database error counting users by role", e);
        }
        return 0;
    }

    @Override
    public List<User> searchUsers(String query, Role role, int limit, int offset) throws DatabaseException {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (role != null) {
            sql.append("AND role = ? ");
            params.add(role.name());
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?) ");
            String wildcard = "%" + query.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
        }
        sql.append("ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(userRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching users", e);
            throw new DatabaseException("Database error searching users", e);
        }
        return list;
    }

    @Override
    public int countSearchUsers(String query, Role role) throws DatabaseException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (role != null) {
            sql.append("AND role = ? ");
            params.add(role.name());
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?) ");
            String wildcard = "%" + query.trim().toLowerCase() + "%";
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
            logger.error("Error counting search users", e);
            throw new DatabaseException("Database error counting search users", e);
        }
        return 0;
    }

    @Override
    public User save(User entity) throws DatabaseException {
        String sql = "INSERT INTO users (name, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getPasswordHash());
            ps.setString(4, entity.getRole().name());
            ps.setString(5, entity.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            logger.error("Error saving user {}", entity.getEmail(), e);
            throw new DatabaseException("Database error saving user", e);
        }
    }

    @Override
    public boolean update(User entity) throws DatabaseException {
        String sql = "UPDATE users SET name = ?, email = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getStatus());
            ps.setInt(4, entity.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user {}", entity.getId(), e);
            throw new DatabaseException("Database error updating user", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user {}", id, e);
            throw new DatabaseException("Database error deleting user", e);
        }
    }

    @Override
    public void updateStatus(int id, String status) throws DatabaseException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating status for user {}", id, e);
            throw new DatabaseException("Database error updating status", e);
        }
    }

    @Override
    public void recordFailedLogin(String email) throws DatabaseException {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1, " +
                "status = CASE WHEN failed_attempts >= 4 THEN 'LOCKED' ELSE status END, " +
                "locked_until = CASE WHEN failed_attempts >= 4 THEN DATE_ADD(NOW(), INTERVAL 15 MINUTE) ELSE locked_until END " +
                "WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error recording failed login for {}", email, e);
            throw new DatabaseException("Database error recording login attempt", e);
        }
    }

    @Override
    public void resetFailedLogins(String email) throws DatabaseException {
        String sql = "UPDATE users SET failed_attempts = 0, locked_until = NULL, " +
                "status = CASE WHEN status = 'LOCKED' THEN 'ACTIVE' ELSE status END " +
                "WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error resetting failed logins for {}", email, e);
            throw new DatabaseException("Database error resetting failed logins", e);
        }
    }

    @Override
    public void updatePassword(int id, String newPasswordHash) throws DatabaseException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating password for user {}", id, e);
            throw new DatabaseException("Database error updating password", e);
        }
    }
}
