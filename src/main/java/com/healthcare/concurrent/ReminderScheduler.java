package com.healthcare.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled background tasks for appointment reminders and periodic analytics refresh.
 */
public final class ReminderScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);
    private static ScheduledExecutorService scheduler;

    private ReminderScheduler() {}

    /**
     * Initializes recurring schedule tasks.
     */
    public static synchronized void startScheduler(Runnable reminderTask, Runnable analyticsRefreshTask) {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "SchedulerWorker-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });

        // Run appointment reminder sweep every 15 minutes (initial delay 1 min)
        if (reminderTask != null) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    logger.info("Executing periodic appointment reminder sweep...");
                    reminderTask.run();
                } catch (Exception e) {
                    logger.error("Error running reminder task", e);
                }
            }, 1, 15, TimeUnit.MINUTES);
        }

        // Run analytics cache refresh every 5 minutes
        if (analyticsRefreshTask != null) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    logger.debug("Executing periodic analytics cache refresh...");
                    analyticsRefreshTask.run();
                } catch (Exception e) {
                    logger.error("Error refreshing analytics cache", e);
                }
            }, 2, 5, TimeUnit.MINUTES);
        }

        logger.info("ReminderScheduler started with 2 recurring tasks.");
    }

    /**
     * Gracefully stops the scheduler.
     */
    public static synchronized void shutdown() {
        if (scheduler != null) {
            logger.info("Shutting down ReminderScheduler...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
    }
}
