package com.healthcare.servlet;

import com.healthcare.model.Notification;
import com.healthcare.model.User;
import com.healthcare.service.NotificationService;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for In-App Notifications, Topbar Bell Dropdown, and Unread Count Badges.
 */
@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications", "/notifications/read", "/notifications/read-all", "/notifications/unread-count"})
public class NotificationServlet extends BaseServlet {
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, ApiResponse.error("Not authenticated"));
            return;
        }

        String path = req.getServletPath();

        try {
            if ("/notifications/unread-count".equals(path)) {
                int count = notificationService.getUnreadCount(user.getId());
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Unread count", count));
                return;
            }

            List<Notification> list = notificationService.getUserNotifications(user.getId(), 20);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Notifications", list));

        } catch (Exception e) {
            logger.error("Error retrieving notifications", e);
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiResponse.error("Error loading notifications"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, ApiResponse.error("Not authenticated"));
            return;
        }

        String path = req.getServletPath();

        try {
            if ("/notifications/read-all".equals(path)) {
                notificationService.markAllAsRead(user.getId());
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("All notifications marked as read"));
            } else if ("/notifications/read".equals(path)) {
                int notifId = Integer.parseInt(req.getParameter("id"));
                notificationService.markAsRead(notifId);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Notification marked as read"));
            }
        } catch (Exception e) {
            logger.error("Error updating notification status", e);
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Failed to update notification"));
        }
    }
}
