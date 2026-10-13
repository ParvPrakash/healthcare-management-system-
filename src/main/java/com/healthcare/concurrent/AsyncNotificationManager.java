package com.healthcare.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages background asynchronous notifications (Email/SMS simulations).
 * Uses a managed ExecutorService fixed thread pool.
 */
public final class AsyncNotificationManager {
    private static final Logger logger = LoggerFactory.getLogger(AsyncNotificationManager.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "NotificationWorker-" + System.currentTimeMillis());
        t.setDaemon(true);
        return t;
    });

    private AsyncNotificationManager() {}

    /**
     * Submits an asynchronous task to dispatch a simulated notification.
     */
    public static void submit(Runnable task) {
        if (!executor.isShutdown()) {
            executor.submit(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    logger.error("Error executing background notification task", e);
                }
            });
        } else {
            logger.warn("Notification executor is shut down; dropping notification task");
        }
    }

    /**
     * Gracefully terminates the executor pool.
     */
    public static void shutdown() {
        logger.info("Shutting down AsyncNotificationManager executor pool...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
