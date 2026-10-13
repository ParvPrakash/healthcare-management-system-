package com.healthcare.servlet;

import com.healthcare.exception.AuthenticationException;
import com.healthcare.exception.ValidationException;
import com.healthcare.model.Patient;
import com.healthcare.model.User;
import com.healthcare.service.AuthService;
import com.healthcare.service.SettingsService;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication Controller handling Login, Registration, and Session Invalidation.
 * Enforces session fixation defense (changeSessionId) and CSRF protection.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends BaseServlet {
    private final AuthService authService = new AuthService();
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();

        if ("/logout".equals(servletPath)) {
            handleLogout(req, resp);
            return;
        }

        User user = getAuthenticatedUser(req);
        if (user != null) {
            redirect(req, resp, user.getDashboardPath());
            return;
        }

        if ("/register".equals(servletPath)) {
            forwardToView(req, resp, "register");
        } else {
            forwardToView(req, resp, "login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();

        if ("/logout".equals(servletPath)) {
            handleLogout(req, resp);
            return;
        }

        if (!validateCsrf(req, resp)) {
            return;
        }

        if ("/login".equals(servletPath)) {
            handleLogin(req, resp);
        } else if ("/register".equals(servletPath)) {
            handleRegister(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String returnUrl = req.getParameter("returnUrl");
        String ipAddress = getClientIp(req);

        try {
            User user = authService.login(email, password, ipAddress);

            // Session Fixation Prevention (§4.7): rotate session ID upon privilege elevation
            HttpSession session = req.getSession(true);
            req.changeSessionId();

            session.setAttribute("USER", user);
            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("USER_NAME", user.getName());
            session.setAttribute("USER_ROLE", user.getRole().name());

            // Set session timeout from dynamic system settings (e.g. 30 mins)
            try {
                int timeoutMins = Integer.parseInt(settingsService.getSetting("session_timeout_minutes", "30"));
                session.setMaxInactiveInterval(timeoutMins * 60);
            } catch (Exception ignored) {}

            logger.info("User {} ({}) authenticated successfully", user.getEmail(), user.getRole());

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Login successful", user.getDashboardPath()));
            } else {
                if (returnUrl != null && !returnUrl.trim().isEmpty() && returnUrl.startsWith("/")) {
                    redirect(req, resp, returnUrl);
                } else {
                    redirect(req, resp, user.getDashboardPath());
                }
            }

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for email: {}: {}", email, e.getMessage());
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("email", email);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, ApiResponse.error(e.getMessage()));
            } else {
                forwardToView(req, resp, "login");
            }
        } catch (Exception e) {
            logger.error("System error during login for: {}", email, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorMessage", "A system error occurred. Please try again later.");
            forwardToView(req, resp, "login");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String phone = req.getParameter("phone");
        String gender = req.getParameter("gender");
        String ageStr = req.getParameter("age");
        String bloodGroup = req.getParameter("bloodGroup");
        String address = req.getParameter("address");
        String emergencyContact = req.getParameter("emergencyContact");

        String clientIp = getClientIp(req);
        if (!com.healthcare.util.RegistrationRateLimiter.tryAcquire(clientIp)) {
            logger.warn("Registration throttled for IP: {}", clientIp);
            resp.setStatus(429);
            req.setAttribute("errorMessage", "Registration rate limit reached (max 5 accounts per hour per IP). Please try again later.");
            forwardToView(req, resp, "register");
            return;
        }

        Patient patient = new Patient();
        patient.setName(name);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setGender(gender);
        if (ageStr != null && !ageStr.trim().isEmpty()) {
            try {
                patient.setAge(Integer.parseInt(ageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }
        patient.setBloodGroup(bloodGroup);
        patient.setAddress(address);
        patient.setEmergencyContact(emergencyContact);

        try {
            Patient registered = authService.registerPatient(patient, password, getClientIp(req));

            // Auto-login or redirect to login
            HttpSession session = req.getSession(true);
            req.changeSessionId();
            session.setAttribute("USER", registered);
            session.setAttribute("USER_ID", registered.getId());
            session.setAttribute("USER_NAME", registered.getName());
            session.setAttribute("USER_ROLE", registered.getRole().name());

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Registration successful", registered.getDashboardPath()));
            } else {
                redirect(req, resp, registered.getDashboardPath() + "?welcome=true");
            }

        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("fieldErrors", e.getFieldErrors());
            req.setAttribute("patient", patient);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error(e.getMessage(), e.getFieldErrors()));
            } else {
                forwardToView(req, resp, "register");
            }
        } catch (Exception e) {
            logger.error("Error registering patient {}", email, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorMessage", "Registration failed due to a database error.");
            req.setAttribute("patient", patient);
            forwardToView(req, resp, "register");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String userName = (String) session.getAttribute("USER_NAME");
            logger.info("User {} logged out, invalidating session {}", userName, session.getId());
            session.invalidate();
        }
        redirect(req, resp, "/login?loggedOut=true");
    }
}
