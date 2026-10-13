package com.healthcare.listener;

import com.healthcare.concurrent.AsyncNotificationManager;
import com.healthcare.concurrent.ReminderScheduler;
import com.healthcare.dao.AppointmentDaoImpl;
import com.healthcare.dao.NotificationDaoImpl;
import com.healthcare.model.Appointment;
import com.healthcare.model.Notification;
import com.healthcare.service.AnalyticsService;
import com.healthcare.service.SettingsService;
import com.healthcare.util.DBConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Lifecycle Listener for the ServletContext.
 * Bootstraps connection pools, background schedulers, and global application configuration.
 */
@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("=======================================================");
        logger.info("Initializing Online Healthcare Management System Context");
        logger.info("=======================================================");

        ServletContext context = sce.getServletContext();

        // 1. Auto-bootstrap database & seed demo data (idempotent)
        try {
            logger.info("Verifying database schema and demo seed data...");
            com.healthcare.util.DatabaseInitializer.initialize();
            boolean dbOk = DBConnection.isHealthy();
            logger.info("Database connectivity health check: {}", dbOk ? "PASSED" : "FAILED");
        } catch (Exception e) {
            logger.error("Database initialization check encountered error", e);
        }

        // 2. Load global clinic settings into application scope
        try {
            SettingsService settingsService = new SettingsService();
            String clinicName = settingsService.getSetting("clinic_name", "CareFirst Health Center");
            context.setAttribute("CLINIC_NAME", clinicName);
            context.setAttribute("APP_VERSION", "1.0.0");
            logger.info("Loaded clinic configuration: {}", clinicName);
        } catch (Exception e) {
            logger.warn("Could not load initial clinic settings", e);
            context.setAttribute("CLINIC_NAME", "CareFirst Health Center");
        }

        // 3. Start background schedulers (Appointment Reminders & Analytics refresh)
        AnalyticsService analyticsService = new AnalyticsService();
        analyticsService.refreshCache();

        ReminderScheduler.startScheduler(
                () -> {
                    // Periodic reminder task
                    try {
                        AppointmentDaoImpl dao = new AppointmentDaoImpl();
                        NotificationDaoImpl notifDao = new NotificationDaoImpl();
                        List<Appointment> todayApps = dao.findTodayAppointments();
                        for (Appointment a : todayApps) {
                            if ("CONFIRMED".equalsIgnoreCase(a.getStatus().name())) {
                                Notification reminder = new Notification(a.getPatientId(),
                                        "Appointment Reminder",
                                        "You have an upcoming appointment today at " + a.getFormattedTime(),
                                        "INFO");
                                notifDao.save(reminder);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error during scheduled reminder execution", e);
                    }
                },
                analyticsService::refreshCache
        );

        logger.info("Healthcare System ServletContext initialized successfully.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Shutting down Healthcare System ServletContext...");

        // 1. Shutdown background schedulers
        ReminderScheduler.shutdown();

        // 2. Shutdown notification thread pool
        AsyncNotificationManager.shutdown();

        // 3. Close database pool
        DBConnection.closePool();

        logger.info("Healthcare System cleanly shut down.");
    }
}
