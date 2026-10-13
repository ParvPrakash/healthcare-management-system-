package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.SystemSetting;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class SettingsDaoImpl implements SettingsDao {
    private static final Logger logger = LoggerFactory.getLogger(SettingsDaoImpl.class);

    private final RowMapper<SystemSetting> settingRowMapper = rs -> {
        SystemSetting s = new SystemSetting();
        s.setKey(rs.getString("setting_key"));
        s.setValue(rs.getString("setting_value"));
        s.setDescription(rs.getString("description"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    };

    @Override
    public Optional<SystemSetting> findById(String key) throws DatabaseException {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(settingRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding setting by key {}", key, e);
            throw new DatabaseException("Database error finding setting", e);
        }
        return Optional.empty();
    }

    @Override
    public List<SystemSetting> findAll() throws DatabaseException {
        String sql = "SELECT * FROM system_settings ORDER BY setting_key ASC";
        List<SystemSetting> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(settingRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all system settings", e);
            throw new DatabaseException("Database error retrieving settings", e);
        }
        return list;
    }

    @Override
    public Map<String, String> getAllSettings() throws DatabaseException {
        // Map / HashMap implementation
        Map<String, String> map = new LinkedHashMap<>();
        for (SystemSetting s : findAll()) {
            map.put(s.getKey(), s.getValue());
        }
        return map;
    }

    @Override
    public String getSetting(String key, String defaultValue) throws DatabaseException {
        return findById(key).map(SystemSetting::getValue).orElse(defaultValue);
    }

    @Override
    public void updateSetting(String key, String value) throws DatabaseException {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating system setting {}", key, e);
            throw new DatabaseException("Database error updating system setting", e);
        }
    }

    @Override
    public void updateSettings(Map<String, String> settings) throws DatabaseException {
        if (settings == null || settings.isEmpty()) return;
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                ps.setString(1, entry.getKey());
                ps.setString(2, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Error batch updating settings", e);
            throw new DatabaseException("Database error batch updating settings", e);
        }
    }

    @Override
    public SystemSetting save(SystemSetting entity) throws DatabaseException {
        updateSetting(entity.getKey(), entity.getValue());
        return entity;
    }

    @Override
    public boolean update(SystemSetting entity) throws DatabaseException {
        updateSetting(entity.getKey(), entity.getValue());
        return true;
    }

    @Override
    public boolean deleteById(String key) throws DatabaseException {
        String sql = "DELETE FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting setting {}", key, e);
            throw new DatabaseException("Database error deleting setting", e);
        }
    }
}
