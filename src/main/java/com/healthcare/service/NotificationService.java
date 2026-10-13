package com.healthcare.service;

import com.healthcare.concurrent.AsyncNotificationManager;
import com.healthcare.dao.NotificationDao;
import com.healthcare.dao.NotificationDaoImpl;
import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service managing user alert notifications and asynchronous dispatching.
 */
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationDao notificationDao;

    public NotificationService() {
        this.notificationDao = new NotificationDaoImpl();
    }

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public List<Notification> getUserNotifications(int userId, int limit) throws DatabaseException {
        return notificationDao.findByUserId(userId, limit);
    }

    public int getUnreadCount(int userId) throws DatabaseException {
        return notificationDao.countUnreadByUserId(userId);
    }

    public void markAllAsRead(int userId) throws DatabaseException {
        notificationDao.markAllRead(userId);
    }

    public void markAsRead(int notificationId) throws DatabaseException {
        notificationDao.markRead(notificationId);
    }

    /**
     * Sends an in-app notification and dispatches background simulated email/SMS asynchronously (§4.3).
     */
    public void sendNotification(int userId, String title, String message, String type) throws DatabaseException {
        Notification notif = new Notification(userId, title, message, type);
        notificationDao.save(notif);

        // Async simulation
        AsyncNotificationManager.submit(() -> {
            logger.info("[ASYNC NOTIFICATION] Dispatched alert to user #{}: {} - {}", userId, title, message);
        });
    }
}
