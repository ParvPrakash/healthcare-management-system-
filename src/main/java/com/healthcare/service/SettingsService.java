package com.healthcare.service;

import com.healthcare.dao.AuditDao;
import com.healthcare.dao.AuditDaoImpl;
import com.healthcare.dao.SettingsDao;
import com.healthcare.dao.SettingsDaoImpl;
import com.healthcare.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Service managing administrative system configuration settings.
 */
public class SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    private final SettingsDao settingsDao;
    private final AuditDao auditDao;

    public SettingsService() {
        this.settingsDao = new SettingsDaoImpl();
        this.auditDao = new AuditDaoImpl();
    }

    public SettingsService(SettingsDao settingsDao, AuditDao auditDao) {
        this.settingsDao = settingsDao;
        this.auditDao = auditDao;
    }

    public Map<String, String> getAllSettings() throws DatabaseException {
        return settingsDao.getAllSettings();
    }

    public String getSetting(String key, String defaultValue) {
        try {
            return settingsDao.getSetting(key, defaultValue);
        } catch (DatabaseException e) {
            logger.error("Error retrieving setting key: {}", key, e);
            return defaultValue;
        }
    }

    public void updateSettings(Map<String, String> settings, Integer adminId, String ipAddress) throws DatabaseException {
        settingsDao.updateSettings(settings);
        auditDao.logAction(adminId, "UPDATE_SETTINGS", "Updated " + settings.size() + " system configuration parameters", ipAddress);
    }
}
