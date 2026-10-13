package com.healthcare.listener;

import com.healthcare.concurrent.MetricsTracker;
import com.healthcare.util.CsrfUtil;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session lifecycle listener tracking active user sessions and initializing session tokens.
 */
@WebListener
public class AppSessionListener implements HttpSessionListener {
    private static final Logger logger = LoggerFactory.getLogger(AppSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        int active = MetricsTracker.incrementActiveSessions();
        // Generate CSRF token on session inception
        CsrfUtil.getOrCreateToken(session);
        logger.debug("Session created: {}. Active count: {}", session.getId(), active);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        int active = MetricsTracker.decrementActiveSessions();
        logger.debug("Session destroyed: {}. Active count: {}", session.getId(), active);
    }
}
