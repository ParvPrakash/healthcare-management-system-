/**
 * Thread management, asynchronous execution, and concurrency coordination.
 * <p>
 * Contains:
 * <ul>
 *   <li>{@link com.healthcare.concurrent.SlotLockManager}: Fine-grained keyed ReentrantLocks preventing race conditions during concurrent appointment slot bookings.</li>
 *   <li>{@link com.healthcare.concurrent.ReminderScheduler}: Background {@link java.util.concurrent.ScheduledExecutorService} delivering automated appointment alerts.</li>
 *   <li>{@link com.healthcare.concurrent.AsyncNotificationManager}: Thread pool managing asynchronous notification dispatches.</li>
 *   <li>{@link com.healthcare.concurrent.MetricsTracker}: High-throughput lock-free concurrency metrics based on atomic counters.</li>
 * </ul>
 * </p>
 */
package com.healthcare.concurrent;
