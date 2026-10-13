package com.healthcare.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * High-concurrency lock coordinator to prevent double-booking collisions.
 * Uses a fine-grained keyed ReentrantLock per doctor-slot stored in a ConcurrentHashMap.
 */
public final class SlotLockManager {
    private static final Logger logger = LoggerFactory.getLogger(SlotLockManager.class);
    private static final ConcurrentHashMap<String, ReentrantLock> slotLocks = new ConcurrentHashMap<>();

    private SlotLockManager() {}

    /**
     * Retrieves or creates a dedicated ReentrantLock for a specific slot.
     */
    private static ReentrantLock getLockForSlot(int slotId) {
        String key = "slot_" + slotId;
        return slotLocks.computeIfAbsent(key, k -> new ReentrantLock(true)); // fair lock
    }

    /**
     * Attempts to acquire the lock for a slot within timeout.
     *
     * @param slotId  Target time slot ID
     * @param timeout Timeout duration
     * @param unit    Time unit
     * @return true if lock acquired, false otherwise
     */
    public static boolean tryLock(int slotId, long timeout, TimeUnit unit) {
        ReentrantLock lock = getLockForSlot(slotId);
        try {
            boolean acquired = lock.tryLock(timeout, unit);
            if (acquired) {
                logger.debug("Acquired lock for slot ID {}", slotId);
            } else {
                logger.warn("Timeout waiting for lock on slot ID {}", slotId);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while acquiring lock for slot ID {}", slotId, e);
            return false;
        }
    }

    /**
     * Releases lock on a slot.
     */
    public static void unlock(int slotId) {
        String key = "slot_" + slotId;
        ReentrantLock lock = slotLocks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            logger.debug("Released lock for slot ID {}", slotId);
        }
    }
}
