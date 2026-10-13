package com.healthcare.util;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe sliding-window rate limiter to throttle account creation per IP.
 * Prevents account spamming when sharing demo links over public tunnels.
 */
public class RegistrationRateLimiter {
    private static final int MAX_REGISTRATIONS_PER_WINDOW = 5;
    private static final long WINDOW_MILLIS = 3600 * 1000L; // 1 hour sliding window

    private static final Map<String, Deque<Long>> ipRegistrationHistory = new ConcurrentHashMap<>();

    /**
     * Attempts to acquire a registration permit for the given IP address.
     *
     * @param ip client IP address
     * @return true if allowed; false if rate-limit exceeded
     */
    public static synchronized boolean tryAcquire(String ip) {
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = "anonymous";
        }

        long now = Instant.now().toEpochMilli();
        Deque<Long> timestamps = ipRegistrationHistory.computeIfAbsent(ip, k -> new ArrayDeque<>());

        // Evict expired timestamps older than the sliding window
        while (!timestamps.isEmpty() && (now - timestamps.peekFirst() > WINDOW_MILLIS)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REGISTRATIONS_PER_WINDOW) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    /**
     * Resets throttling history (useful during testing or reset).
     */
    public static synchronized void reset() {
        ipRegistrationHistory.clear();
    }
}
