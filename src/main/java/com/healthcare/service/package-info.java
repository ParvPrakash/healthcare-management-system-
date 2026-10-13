/**
 * Business logic and transaction orchestration layer.
 * <p>
 * Services encapsulate domain rules, input validation, role-based workflows,
 * thread-safe caching (e.g., {@link com.healthcare.service.DoctorCache}),
 * and database transactions across multiple DAO invocations.
 * Examples include {@link com.healthcare.service.AuthService},
 * {@link com.healthcare.service.AppointmentService}, and {@link com.healthcare.service.AnalyticsService}.
 * </p>
 */
package com.healthcare.service;
