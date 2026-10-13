package com.healthcare.concurrent;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe live metric counters using AtomicInteger and ConcurrentHashMap.
 */
public final class MetricsTracker {
    private static final AtomicInteger activeSessionsCount = new AtomicInteger(0);
    private static final AtomicInteger bookingsTodayCount = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, AtomicInteger> endpointHits = new ConcurrentHashMap<>();

    private MetricsTracker() {}

    public static int incrementActiveSessions() {
        return activeSessionsCount.incrementAndGet();
    }

    public static int decrementActiveSessions() {
        int current = activeSessionsCount.decrementAndGet();
        if (current < 0) {
            activeSessionsCount.set(0);
            return 0;
        }
        return current;
    }

    public static int getActiveSessions() {
        return activeSessionsCount.get();
    }

    public static void setBookingsToday(int count) {
        bookingsTodayCount.set(count);
    }

    public static int incrementBookingsToday() {
        return bookingsTodayCount.incrementAndGet();
    }

    public static int getBookingsToday() {
        return bookingsTodayCount.get();
    }

    public static void recordEndpointHit(String endpoint) {
        if (endpoint != null) {
            endpointHits.computeIfAbsent(endpoint, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    public static Map<String, AtomicInteger> getEndpointHits() {
        return Collections.unmodifiableMap(endpointHits);
    }
}
