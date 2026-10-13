package com.healthcare.servlet;

import com.healthcare.model.User;
import com.healthcare.service.SettingsService;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Administrative System Configuration & Clinic Settings.
 */
@WebServlet(name = "SettingsServlet", urlPatterns = {"/admin/settings", "/admin/reset-demo"})
public class SettingsServlet extends BaseServlet {
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, String> settings = settingsService.getAllSettings();
            req.setAttribute("settings", settings);
        } catch (Exception e) {
            logger.error("Error loading system settings", e);
            req.setAttribute("errorMessage", "Error loading system settings: " + e.getMessage());
        }

        forwardToView(req, resp, "admin/settings");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;

        String path = req.getServletPath();
        if ("/admin/reset-demo".equals(path)) {
            handleResetDemo(req, resp);
            return;
        }

        User admin = getAuthenticatedUser(req);
        String ip = getClientIp(req);

        Map<String, String> updateMap = new HashMap<>();
        String[] keys = {
                "clinic_name",
                "clinic_phone",
                "clinic_email",
                "clinic_address",
                "slot_duration_minutes",
                "working_hours_start",
                "working_hours_end",
                "session_timeout_minutes",
                "max_bookings_per_day",
                "notifications_enabled",
                "maintenance_mode"
        };

        for (String key : keys) {
            String val = req.getParameter(key);
            if (val != null) {
                updateMap.put(key, val.trim());
            }
        }

        try {
            settingsService.updateSettings(updateMap, admin != null ? admin.getId() : null, ip);

            // Update application scope if clinic_name changed
            if (updateMap.containsKey("clinic_name")) {
                req.getServletContext().setAttribute("CLINIC_NAME", updateMap.get("clinic_name"));
            }

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("System settings updated successfully!"));
            } else {
                req.setAttribute("successMessage", "Settings saved successfully!");
                doGet(req, resp);
            }
        } catch (Exception e) {
            logger.error("Failed to update system settings", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiResponse.error("Failed to update settings: " + e.getMessage()));
            } else {
                req.setAttribute("errorMessage", "Error updating settings: " + e.getMessage());
                doGet(req, resp);
            }
        }
    }

    private void handleResetDemo(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            logger.info("Admin initiated demo database reset...");
            com.healthcare.util.DatabaseInitializer.resetDemoData();
            com.healthcare.util.RegistrationRateLimiter.reset();

            // Refresh settings in context
            String clinicName = settingsService.getSetting("clinic_name", "CareFirst Health Center");
            req.getServletContext().setAttribute("CLINIC_NAME", clinicName);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Demo database has been successfully reset!"));
            } else {
                redirect(req, resp, "/admin/settings?demoReset=true");
            }
        } catch (Exception e) {
            logger.error("Error executing demo reset", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiResponse.error("Reset failed: " + e.getMessage()));
            } else {
                req.setAttribute("errorMessage", "Demo reset failed: " + e.getMessage());
                doGet(req, resp);
            }
        }
    }
}
