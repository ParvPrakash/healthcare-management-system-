package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.AuditLog;

import java.sql.Connection;
import java.util.List;

public interface AuditDao extends Repository<AuditLog, Integer> {
    List<AuditLog> findRecent(int limit) throws DatabaseException;
    List<AuditLog> searchLogs(String query, int limit, int offset) throws DatabaseException;
    int countSearchLogs(String query) throws DatabaseException;
    void logAction(Integer userId, String action, String details, String ipAddress) throws DatabaseException;
    void logAction(Connection conn, Integer userId, String action, String details, String ipAddress) throws DatabaseException;
}
