/**
 * Servlet Filter interceptor chain for cross-cutting web concerns.
 * <p>
 * Implements filters for:
 * <ul>
 *   <li>{@link com.healthcare.filter.EncodingFilter}: Enforces UTF-8 character encoding on all requests and responses.</li>
 *   <li>{@link com.healthcare.filter.LoggingFilter}: Measures execution duration and logs HTTP access patterns with SLF4J.</li>
 *   <li>{@link com.healthcare.filter.AuthFilter}: Guards private routes against unauthenticated requests.</li>
 *   <li>{@link com.healthcare.filter.RoleFilter}: Enforces strict Role-Based Access Control (RBAC) across Admin, Doctor, and Patient views.</li>
 *   <li>{@link com.healthcare.filter.TunnelSecurityFilter}: Adapts secure cookie attributes and protocol schemes over reverse-proxy tunnels.</li>
 * </ul>
 * </p>
 */
package com.healthcare.filter;
