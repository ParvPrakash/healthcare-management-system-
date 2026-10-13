package com.healthcare.servlet;

import com.healthcare.model.Doctor;
import com.healthcare.model.Reportable;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.service.UserService;
import com.healthcare.util.ApiResponse;
import com.healthcare.util.CsvExporter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin User Management Controller.
 * Handles user listings, search, role filters, doctor creation, status toggles, and CSV exports.
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/admin/users", "/admin/users/create", "/admin/users/status", "/admin/users/delete", "/admin/users/export"})
public class UserServlet extends BaseServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/admin/users/export".equals(path)) {
            handleExportCsv(req, resp);
            return;
        }

        String query = req.getParameter("q");
        String roleStr = req.getParameter("role");
        String pageStr = req.getParameter("page");

        Role roleFilter = (roleStr != null && !roleStr.isEmpty() && !"ALL".equalsIgnoreCase(roleStr))
                ? Role.fromString(roleStr) : null;

        int page = 1;
        int pageSize = 10;
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr));
            } catch (NumberFormatException ignored) {}
        }

        try {
            List<User> users = userService.searchUsers(query, roleFilter, page, pageSize);
            int totalCount = userService.countSearchUsers(query, roleFilter);
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            req.setAttribute("users", users);
            req.setAttribute("query", query != null ? query : "");
            req.setAttribute("roleFilter", roleStr != null ? roleStr : "ALL");
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalCount", totalCount);

        } catch (Exception e) {
            logger.error("Error retrieving user list", e);
            req.setAttribute("errorMessage", "Error fetching user list: " + e.getMessage());
        }

        forwardToView(req, resp, "admin/users");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;

        String path = req.getServletPath();
        User admin = getAuthenticatedUser(req);
        String ip = getClientIp(req);

        if ("/admin/users/create".equals(path)) {
            handleCreateDoctor(req, resp, admin, ip);
        } else if ("/admin/users/status".equals(path)) {
            handleToggleStatus(req, resp, admin, ip);
        } else if ("/admin/users/delete".equals(path)) {
            handleDeleteUser(req, resp, admin, ip);
        } else {
            doGet(req, resp);
        }
    }

    private void handleCreateDoctor(HttpServletRequest req, HttpServletResponse resp, User admin, String ip)
            throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String specialization = req.getParameter("specialization");
        String expStr = req.getParameter("experience");
        String feeStr = req.getParameter("consultationFee");
        String room = req.getParameter("roomNumber");
        String bio = req.getParameter("bio");

        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setSpecialization(specialization);
        try {
            doctor.setExperience(expStr != null ? Integer.parseInt(expStr) : 1);
        } catch (NumberFormatException ignored) {}
        try {
            doctor.setConsultationFee(feeStr != null ? new BigDecimal(feeStr) : BigDecimal.valueOf(50.00));
        } catch (Exception ignored) {}
        doctor.setRoomNumber(room);
        doctor.setBio(bio);

        try {
            userService.createDoctor(doctor, password, admin.getId(), ip);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Doctor created successfully"));
            } else {
                redirect(req, resp, "/admin/users?success=created");
            }
        } catch (Exception e) {
            logger.error("Failed to create doctor", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error(e.getMessage()));
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                doGet(req, resp);
            }
        }
    }

    private void handleToggleStatus(HttpServletRequest req, HttpServletResponse resp, User admin, String ip)
            throws IOException {
        try {
            int userId = Integer.parseInt(req.getParameter("id"));
            String status = req.getParameter("status");
            userService.updateUserStatus(userId, status, admin.getId(), ip);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("User status updated to " + status));
            } else {
                redirect(req, resp, "/admin/users?success=status_updated");
            }
        } catch (Exception e) {
            logger.error("Error updating user status", e);
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Failed to update status"));
        }
    }

    private void handleDeleteUser(HttpServletRequest req, HttpServletResponse resp, User admin, String ip)
            throws IOException {
        try {
            int userId = Integer.parseInt(req.getParameter("id"));
            userService.deleteUser(userId, admin.getId(), ip);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("User deleted successfully"));
            } else {
                redirect(req, resp, "/admin/users?success=deleted");
            }
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Failed to delete user"));
        }
    }

    private void handleExportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<User> users = userService.searchUsers(null, null, 1, 1000);
            List<String> headers = Arrays.asList("ID", "Name", "Email", "Role", "Status", "Created At");

            List<Reportable> reportables = users.stream().map(u -> new Reportable() {
                @Override
                public String getReportIdentifier() {
                    return String.valueOf(u.getId());
                }

                @Override
                public String getReportTitle() {
                    return u.getName();
                }

                @Override
                public Map<String, Object> toReportMap() {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("role", u.getRole().name());
                    m.put("status", u.getStatus());
                    return m;
                }

                @Override
                public String toCsvRow() {
                    return String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                            u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.getStatus(), u.getCreatedAt());
                }
            }).collect(Collectors.toList());

            String csvContent = CsvExporter.exportToCsv(headers, reportables);

            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"users_export.csv\"");
            resp.getWriter().write(csvContent);
            resp.getWriter().flush();

        } catch (Exception e) {
            logger.error("Error exporting users to CSV", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSV Export failed");
        }
    }
}
