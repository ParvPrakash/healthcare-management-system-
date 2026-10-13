/**
 * Cross-layer helper utilities and infrastructural building blocks.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link com.healthcare.util.DBConnection}: High-performance HikariCP connection pooling manager.</li>
 *   <li>{@link com.healthcare.util.PasswordUtil}: BCrypt salted password hashing and verification.</li>
 *   <li>{@link com.healthcare.util.CsrfUtil}: Cryptographic session-bound anti-CSRF token verification.</li>
 *   <li>{@link com.healthcare.util.ApiResponse}: Standardized JSON envelopes for AJAX/REST endpoints.</li>
 *   <li>{@link com.healthcare.util.DatabaseInitializer}: Automated schema creation and demo seeding runner.</li>
 *   <li>{@link com.healthcare.util.PdfReportGenerator}: OpenPDF-based clinical encounter report creation.</li>
 *   <li>{@link com.healthcare.util.CsvExporter}: Appointment and audit log export formatting.</li>
 *   <li>{@link com.healthcare.util.RegistrationRateLimiter}: Token-bucket IP registration throttle.</li>
 * </ul>
 * </p>
 */
package com.healthcare.util;
