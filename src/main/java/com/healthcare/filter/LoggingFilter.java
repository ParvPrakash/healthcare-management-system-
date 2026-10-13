package com.healthcare.filter;

import com.healthcare.concurrent.MetricsTracker;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filter logging all incoming requests, measuring latency, and recording endpoint metrics.
 */
@WebFilter(filterName = "LoggingFilter", urlPatterns = "/*")
public class LoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("LoggingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String method = req.getMethod();
        long startTime = System.currentTimeMillis();

        MetricsTracker.recordEndpointHit(uri);

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (!uri.startsWith("/assets/")) {
                logger.info("[HTTP {}] {} -> status {} ({} ms)", method, uri, res.getStatus(), duration);
            }
        }
    }

    @Override
    public void destroy() {
        logger.info("LoggingFilter destroyed");
    }
}
