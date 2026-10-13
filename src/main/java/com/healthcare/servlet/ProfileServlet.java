package com.healthcare.servlet;

import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.service.AuthService;
import com.healthcare.service.UserService;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Profile Controller handling profile viewing, updating, and password changes.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile", "/profile/password"})
public class ProfileServlet extends BaseServlet {
    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            redirect(req, resp, "/login");
            return;
        }

        try {
            if (user.getRole() == Role.DOCTOR) {
                Doctor doc = userService.getDoctorById(user.getId()).orElse((Doctor) user);
                req.setAttribute("profileUser", doc);
            } else if (user.getRole() == Role.PATIENT) {
                Patient pat = userService.getPatientById(user.getId()).orElse((Patient) user);
                req.setAttribute("profileUser", pat);
            } else {
                req.setAttribute("profileUser", user);
            }
        } catch (Exception e) {
            logger.error("Error loading user profile", e);
        }

        forwardToView(req, resp, "patient/profile");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;

        User user = getAuthenticatedUser(req);
        if (user == null) {
            redirect(req, resp, "/login");
            return;
        }

        String path = req.getServletPath();
        String ip = getClientIp(req);

        if ("/profile/password".equals(path)) {
            handleChangePassword(req, resp, user, ip);
        } else {
            handleUpdateProfile(req, resp, user, ip);
        }
    }

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp, User user, String ip)
            throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");

        user.setName(name);
        user.setEmail(email);

        try {
            if (user.getRole() == Role.PATIENT) {
                Patient p = userService.getPatientById(user.getId()).orElse(new Patient());
                p.setName(name);
                p.setEmail(email);
                p.setPhone(req.getParameter("phone"));
                p.setGender(req.getParameter("gender"));
                p.setBloodGroup(req.getParameter("bloodGroup"));
                p.setAddress(req.getParameter("address"));
                p.setEmergencyContact(req.getParameter("emergencyContact"));
                userService.updatePatientProfile(p, ip);
                req.getSession().setAttribute("USER", p);
            } else if (user.getRole() == Role.DOCTOR) {
                Doctor d = userService.getDoctorById(user.getId()).orElse(new Doctor());
                d.setName(name);
                d.setEmail(email);
                d.setSpecialization(req.getParameter("specialization"));
                d.setRoomNumber(req.getParameter("roomNumber"));
                d.setBio(req.getParameter("bio"));
                String feeStr = req.getParameter("consultationFee");
                if (feeStr != null && !feeStr.isEmpty()) {
                    d.setConsultationFee(new BigDecimal(feeStr));
                }
                userService.updateDoctorProfile(d, ip);
                req.getSession().setAttribute("USER", d);
            }

            req.getSession().setAttribute("USER_NAME", user.getName());

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Profile updated successfully"));
            } else {
                req.setAttribute("successMessage", "Profile updated successfully!");
                doGet(req, resp);
            }
        } catch (Exception e) {
            logger.error("Failed to update profile", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiResponse.error("Failed to update profile: " + e.getMessage()));
            } else {
                req.setAttribute("errorMessage", "Error updating profile: " + e.getMessage());
                doGet(req, resp);
            }
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, User user, String ip)
            throws ServletException, IOException {
        String oldPass = req.getParameter("oldPassword");
        String newPass = req.getParameter("newPassword");

        try {
            authService.changePassword(user.getId(), oldPass, newPass, ip);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Password changed successfully"));
            } else {
                req.setAttribute("successMessage", "Password updated successfully!");
                doGet(req, resp);
            }
        } catch (Exception e) {
            logger.warn("Password change failed for user {}: {}", user.getId(), e.getMessage());
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error(e.getMessage()));
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                doGet(req, resp);
            }
        }
    }
}
