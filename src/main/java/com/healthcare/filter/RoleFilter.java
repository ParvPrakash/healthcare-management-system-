package com.healthcare.filter;

import com.healthcare.model.Role;
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
 * Route guard enforcing role-based access control (RBAC).
 */
@WebFilter(filterName = "RoleFilter", urlPatterns = {"/admin/*", "/doctor/*", "/patient/*"})
public class RoleFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RoleFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("RoleFilter initialized for RBAC route enforcement");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("USER") : null;

        if (user == null) {
            chain.doFilter(request, response);
            return;
        }

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        boolean authorized = true;
        if (path.startsWith("/admin") && user.getRole() != Role.ADMIN) {
            authorized = false;
        } else if (path.startsWith("/doctor") && user.getRole() != Role.DOCTOR) {
            authorized = false;
        } else if (path.startsWith("/patient") && user.getRole() != Role.PATIENT) {
            authorized = false;
        }

        if (!authorized) {
            logger.warn("Access denied: User {} with role {} attempted accessing {}", user.getEmail(), user.getRole(), path);
            res.sendRedirect(contextPath + user.getDashboardPath() + "?error=access_denied");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("RoleFilter destroyed");
    }
}
