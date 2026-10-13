package com.healthcare.servlet;

import com.healthcare.concurrent.MetricsTracker;
import com.healthcare.dao.*;
import com.healthcare.model.*;
import com.healthcare.service.AnalyticsService;
import com.healthcare.service.AppointmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller routing and populating the dedicated role dashboards:
 * Admin Dashboard, Doctor Dashboard, and Patient Dashboard.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/admin/dashboard", "/doctor/dashboard", "/patient/dashboard"})
public class DashboardServlet extends BaseServlet {
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final AppointmentDao appointmentDao = new AppointmentDaoImpl();
    private final FeedbackDao feedbackDao = new FeedbackDaoImpl();
    private final AuditDao auditDao = new AuditDaoImpl();
    private final MedicalRecordDao recordDao = new MedicalRecordDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            redirect(req, resp, "/login");
            return;
        }

        String path = req.getServletPath();

        try {
            if ("/admin/dashboard".equals(path)) {
                renderAdminDashboard(req, resp);
            } else if ("/doctor/dashboard".equals(path)) {
                renderDoctorDashboard(req, resp, user);
            } else if ("/patient/dashboard".equals(path)) {
                renderPatientDashboard(req, resp, user);
            } else {
                redirect(req, resp, user.getDashboardPath());
            }
        } catch (Exception e) {
            logger.error("Error loading dashboard for path {}", path, e);
            req.setAttribute("errorMessage", "Error assembling dashboard: " + e.getMessage());
            forwardToView(req, resp, "error");
        }
    }

    private void renderAdminDashboard(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Map<String, Object> stats = analyticsService.getOverviewStats();
        List<Appointment> todayApps = appointmentDao.findTodayAppointments();
        List<AuditLog> recentLogs = auditDao.findRecent(6);
        String chartJson = analyticsService.getChartDataJson(7);

        req.setAttribute("stats", stats);
        req.setAttribute("todayAppointments", todayApps);
        req.setAttribute("recentLogs", recentLogs);
        req.setAttribute("chartData", chartJson);
        req.setAttribute("liveSessions", MetricsTracker.getActiveSessions());
        req.setAttribute("bookingsToday", MetricsTracker.getBookingsToday());

        forwardToView(req, resp, "admin/dashboard");
    }

    private void renderDoctorDashboard(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
        List<Appointment> upcoming = appointmentService.getDoctorUpcomingAppointments(user.getId(), 5);
        List<Appointment> today = appointmentDao.searchAppointments(user.getId(), LocalDate.now());
        List<Feedback> recentFeedback = feedbackDao.findByDoctorId(user.getId());
        double avgRating = feedbackDao.getAverageRating(user.getId());

        req.setAttribute("upcomingAppointments", upcoming);
        req.setAttribute("todayAppointments", today);
        req.setAttribute("recentFeedback", recentFeedback);
        req.setAttribute("avgRating", Math.round(avgRating * 10.0) / 10.0);
        req.setAttribute("totalReviews", recentFeedback.size());

        forwardToView(req, resp, "doctor/dashboard");
    }

    private void renderPatientDashboard(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
        List<Appointment> appointments = appointmentService.getPatientAppointments(user.getId(), null);

        // Find next confirmed appointment
        Appointment nextApp = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.RESCHEDULED)
                .filter(a -> !a.getAppointmentDate().isBefore(LocalDate.now()))
                .min(Appointment::compareTo)
                .orElse(null);

        List<MedicalRecord> recentRecords = recordDao.findByPatientId(user.getId());

        req.setAttribute("appointments", appointments);
        req.setAttribute("nextAppointment", nextApp);
        req.setAttribute("recentRecords", recentRecords);

        forwardToView(req, resp, "patient/dashboard");
    }
}
