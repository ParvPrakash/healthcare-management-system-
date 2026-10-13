/**
 * Web application lifecycle and session event listeners.
 * <p>
 * Contains:
 * <ul>
 *   <li>{@link com.healthcare.listener.AppContextListener}: Initializes application singletons,
 *       starts background schedulers, migrates database schemas, and safely releases HikariCP connection pools.</li>
 *   <li>{@link com.healthcare.listener.AppSessionListener}: Tracks active HTTP sessions and logs session lifecycle transitions.</li>
 * </ul>
 * </p>
 */
package com.healthcare.listener;
