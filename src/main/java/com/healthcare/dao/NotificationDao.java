package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Notification;

import java.sql.Connection;
import java.util.List;

public interface NotificationDao extends Repository<Notification, Integer> {
    List<Notification> findByUserId(int userId, int limit) throws DatabaseException;
    int countUnreadByUserId(int userId) throws DatabaseException;
    void markAllRead(int userId) throws DatabaseException;
    void markRead(int notificationId) throws DatabaseException;
    Notification saveTransactional(Connection conn, Notification notification) throws DatabaseException;
}
