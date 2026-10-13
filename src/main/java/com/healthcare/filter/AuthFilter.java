package com.healthcare.filter;

import com.healthcare.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filter checking authentication state for protected URL routes.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/admin/*", "/doctor/*", "/patient/*", "/profile/*", "/notifications/*"})
public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("AuthFilter initialized for protected resource routes");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("USER") : null;

        if (currentUser == null) {
            String uri = req.getRequestURI();
            String contextPath = req.getContextPath();
            String targetPath = uri.substring(contextPath.length());
            String query = req.getQueryString();
            if (query != null && !query.isEmpty()) {
                targetPath += "?" + query;
            }

            logger.warn("Unauthenticated access attempt to {}. Redirecting to /login", targetPath);
            res.sendRedirect(contextPath + "/login?returnUrl=" + java.net.URLEncoder.encode(targetPath, java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed");
    }
}
