package com.healthcare.servlet;

import com.healthcare.dao.AppointmentDao;
import com.healthcare.dao.AppointmentDaoImpl;
import com.healthcare.dao.MedicalRecordDao;
import com.healthcare.dao.MedicalRecordDaoImpl;
import com.healthcare.model.*;
import com.healthcare.service.AppointmentService;
import com.healthcare.util.ApiResponse;
import com.healthcare.util.CsvExporter;
import com.healthcare.util.PdfReportGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controller for comprehensive Appointment lifecycles:
 * Booking, Rescheduling, Cancelling, Completing, PDF Summaries, and CSV Reporting.
 */
@WebServlet(
        name = "AppointmentServlet",
        urlPatterns = {
                "/patient/appointments",
                "/doctor/appointments",
                "/admin/appointments",
                "/appointments/book",
                "/appointments/reschedule",
                "/appointments/cancel",
                "/appointments/complete",
                "/appointments/pdf",
                "/admin/appointments/export"
        }
)
public class AppointmentServlet extends BaseServlet {
    private final AppointmentService appointmentService = new AppointmentService();
    private final AppointmentDao appointmentDao = new AppointmentDaoImpl();
    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/appointments/pdf".equals(path)) {
            handleDownloadPdf(req, resp);
            return;
        }

        if ("/admin/appointments/export".equals(path)) {
            handleExportCsv(req, resp);
            return;
        }

        User user = getAuthenticatedUser(req);
        if (user == null) {
            redirect(req, resp, "/login");
            return;
        }

        try {
            if (user.getRole() == Role.PATIENT) {
                String statusStr = req.getParameter("status");
                AppointmentStatus status = (statusStr != null && !statusStr.isEmpty() && !"ALL".equalsIgnoreCase(statusStr))
                        ? AppointmentStatus.fromString(statusStr) : null;

                List<Appointment> appointments = appointmentService.getPatientAppointments(user.getId(), status);
                req.setAttribute("appointments", appointments);
                req.setAttribute("selectedStatus", statusStr != null ? statusStr : "ALL");
                forwardToView(req, resp, "patient/appointments");

            } else if (user.getRole() == Role.DOCTOR) {
                List<Appointment> upcoming = appointmentService.getDoctorUpcomingAppointments(user.getId(), 50);
                List<Appointment> past = appointmentService.getDoctorPastAppointments(user.getId(), 50);

                req.setAttribute("upcomingAppointments", upcoming);
                req.setAttribute("pastAppointments", past);
                forwardToView(req, resp, "doctor/appointments");

            } else if (user.getRole() == Role.ADMIN) {
                String query = req.getParameter("q");
                String statusStr = req.getParameter("status");
                String pageStr = req.getParameter("page");

                AppointmentStatus status = (statusStr != null && !statusStr.isEmpty() && !"ALL".equalsIgnoreCase(statusStr))
                        ? AppointmentStatus.fromString(statusStr) : null;

                int page = 1;
                int pageSize = 15;
                if (pageStr != null && !pageStr.isEmpty()) {
                    try {
                        page = Math.max(1, Integer.parseInt(pageStr));
                    } catch (NumberFormatException ignored) {}
                }

                List<Appointment> appointments = appointmentService.searchAllAppointments(query, status, page, pageSize);
                int totalCount = appointmentDao.countTotal();
                int totalPages = (int) Math.ceil((double) totalCount / pageSize);

                req.setAttribute("appointments", appointments);
                req.setAttribute("query", query != null ? query : "");
                req.setAttribute("statusFilter", statusStr != null ? statusStr : "ALL");
                req.setAttribute("currentPage", page);
                req.setAttribute("totalPages", totalPages);
                forwardToView(req, resp, "admin/appointments");
            }
        } catch (Exception e) {
            logger.error("Error loading appointments", e);
            req.setAttribute("errorMessage", "Error loading appointments: " + e.getMessage());
            forwardToView(req, resp, "patient/appointments");
        }
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

        switch (path) {
            case "/appointments/book":
                handleBookAppointment(req, resp, user, ip);
                break;
            case "/appointments/reschedule":
                handleRescheduleAppointment(req, resp, user, ip);
                break;
            case "/appointments/cancel":
                handleCancelAppointment(req, resp, user, ip);
                break;
            case "/appointments/complete":
                handleCompleteAppointment(req, resp, user, ip);
                break;
            default:
                doGet(req, resp);
        }
    }

    private void handleBookAppointment(HttpServletRequest req, HttpServletResponse resp, User user, String ip)
            throws IOException {
        try {
            int doctorId = Integer.parseInt(req.getParameter("doctorId"));
            int slotId = Integer.parseInt(req.getParameter("slotId"));
            String reason = req.getParameter("reason");
            if (reason == null || reason.trim().isEmpty()) {
                reason = req.getParameter("reasonForVisit");
            }

            int patientId = (user.getRole() == Role.PATIENT) ? user.getId() : Integer.parseInt(req.getParameter("patientId"));

            Appointment booked = appointmentService.bookAppointment(patientId, doctorId, slotId, reason, ip);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Appointment successfully booked!", booked));
            } else {
                redirect(req, resp, "/patient/appointments?success=booked");
            }

        } catch (Exception e) {
            logger.warn("Booking failed: {}", e.getMessage());
            int statusCode = (e.getMessage().contains("booked") || e.getMessage().contains("conflict"))
                    ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_BAD_REQUEST;

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, statusCode, ApiResponse.error(e.getMessage()));
            } else {
                redirect(req, resp, "/patient/doctors?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        }
    }

    private void handleRescheduleAppointment(HttpServletRequest req, HttpServletResponse resp, User user, String ip)
            throws IOException {
        try {
            int appointmentId = Integer.parseInt(req.getParameter("appointmentId"));
            int newSlotId = Integer.parseInt(req.getParameter("newSlotId"));

            appointmentService.rescheduleAppointment(appointmentId, newSlotId, user.getId(), ip);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Appointment rescheduled successfully!"));
            } else {
                redirect(req, resp, user.getDashboardPath() + "?success=rescheduled");
            }
        } catch (Exception e) {
            logger.error("Reschedule failed", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_CONFLICT, ApiResponse.error(e.getMessage()));
            } else {
                redirect(req, resp, user.getDashboardPath() + "?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        }
    }

    private void handleCancelAppointment(HttpServletRequest req, HttpServletResponse resp, User user, String ip)
            throws IOException {
        try {
            int appointmentId = Integer.parseInt(req.getParameter("appointmentId"));
            String reason = req.getParameter("reason");
            if (reason == null || reason.trim().isEmpty()) {
                reason = "Cancelled by " + user.getName();
            }

            appointmentService.cancelAppointment(appointmentId, reason, user.getId(), ip);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Appointment cancelled"));
            } else {
                redirect(req, resp, user.getDashboardPath() + "?success=cancelled");
            }
        } catch (Exception e) {
            logger.error("Cancellation failed", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error(e.getMessage()));
            } else {
                redirect(req, resp, user.getDashboardPath() + "?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        }
    }

    private void handleCompleteAppointment(HttpServletRequest req, HttpServletResponse resp, User user, String ip)
            throws IOException {
        try {
            int appointmentId = Integer.parseInt(req.getParameter("appointmentId"));
            appointmentService.completeAppointment(appointmentId, user.getId(), ip);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Appointment marked as completed"));
            } else {
                redirect(req, resp, "/doctor/appointments?success=completed");
            }
        } catch (Exception e) {
            logger.error("Error completing appointment", e);
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Failed to complete appointment"));
        }
    }

    private void handleDownloadPdf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Appointment ID required");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Optional<Appointment> appOpt = appointmentService.getAppointmentById(id);
            if (appOpt.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                return;
            }

            Appointment appointment = appOpt.get();
            MedicalRecord record = medicalRecordDao.findByAppointmentId(id).orElse(null);

            String clinicName = (String) req.getServletContext().getAttribute("CLINIC_NAME");
            byte[] pdfBytes = PdfReportGenerator.generateAppointmentPdf(appointment, record, clinicName);

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"Appointment_Summary_" + id + ".pdf\"");
            resp.setContentLength(pdfBytes.length);

            try (OutputStream os = resp.getOutputStream()) {
                os.write(pdfBytes);
                os.flush();
            }

        } catch (Exception e) {
            logger.error("Error generating PDF summary", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "PDF Generation failed");
        }
    }

    private void handleExportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Appointment> list = appointmentDao.findAll();
            List<String> headers = Arrays.asList("Appointment ID", "Doctor", "Patient", "Date", "Time", "Status", "Reason");
            String csv = CsvExporter.exportToCsv(headers, list);

            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"appointments_report.csv\"");
            resp.getWriter().write(csv);
            resp.getWriter().flush();

        } catch (Exception e) {
            logger.error("Error exporting appointments to CSV", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSV Export failed");
        }
    }
}
