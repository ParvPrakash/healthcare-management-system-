/**
 * Domain-specific custom exception hierarchy.
 * <p>
 * Forms a structured hierarchy inheriting from {@link com.healthcare.exception.HealthcareException}:
 * <ul>
 *   <li>{@link com.healthcare.exception.AuthenticationException}: Invalid credentials, inactive account, or lockout.</li>
 *   <li>{@link com.healthcare.exception.ValidationException}: Form and business invariant input validation failures.</li>
 *   <li>{@link com.healthcare.exception.DatabaseException}: JDBC / SQL transaction failures and connection issues.</li>
 *   <li>{@link com.healthcare.exception.ResourceNotFoundException}: Missing entities (doctor, patient, appointment).</li>
 *   <li>{@link com.healthcare.exception.AppointmentConflictException}: Slot contention and concurrent booking clashes.</li>
 * </ul>
 * </p>
 */
package com.healthcare.exception;
