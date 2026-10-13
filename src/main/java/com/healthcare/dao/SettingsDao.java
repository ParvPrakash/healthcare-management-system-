package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.SystemSetting;

import java.util.Map;

public interface SettingsDao extends Repository<SystemSetting, String> {
    Map<String, String> getAllSettings() throws DatabaseException;
    String getSetting(String key, String defaultValue) throws DatabaseException;
    void updateSetting(String key, String value) throws DatabaseException;
    void updateSettings(Map<String, String> settings) throws DatabaseException;
}
