package com.healthcare.servlet;

import com.healthcare.dao.FeedbackDao;
import com.healthcare.dao.FeedbackDaoImpl;
import com.healthcare.model.Feedback;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for patient feedback reviews and doctor rating distribution.
 */
@WebServlet(name = "FeedbackServlet", urlPatterns = {"/patient/feedback", "/doctor/feedback"})
public class FeedbackServlet extends BaseServlet {
    private final FeedbackDao feedbackDao = new FeedbackDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            redirect(req, resp, "/login");
            return;
        }

        try {
            if (user.getRole() == Role.DOCTOR) {
                List<Feedback> feedbackList = feedbackDao.findByDoctorId(user.getId());
                Map<Integer, Integer> distribution = feedbackDao.getRatingDistribution(user.getId());
                double avgRating = feedbackDao.getAverageRating(user.getId());

                req.setAttribute("feedbackList", feedbackList);
                req.setAttribute("distribution", distribution);
                req.setAttribute("avgRating", Math.round(avgRating * 10.0) / 10.0);
                req.setAttribute("totalReviews", feedbackList.size());

                forwardToView(req, resp, "doctor/feedback");
            } else {
                redirect(req, resp, "/patient/appointments");
            }
        } catch (Exception e) {
            logger.error("Error retrieving feedback", e);
            req.setAttribute("errorMessage", "Error loading feedback: " + e.getMessage());
            forwardToView(req, resp, "doctor/feedback");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;

        User user = getAuthenticatedUser(req);
        if (user == null || user.getRole() != Role.PATIENT) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            int appointmentId = Integer.parseInt(req.getParameter("appointmentId"));
            int doctorId = Integer.parseInt(req.getParameter("doctorId"));
            int rating = Integer.parseInt(req.getParameter("rating"));
            String comment = req.getParameter("comment");

            Feedback fb = new Feedback(null, appointmentId, doctorId, user.getId(), rating, comment);
            feedbackDao.save(fb);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Thank you for your feedback!"));
            } else {
                redirect(req, resp, "/patient/appointments?success=feedback_submitted");
            }
        } catch (Exception e) {
            logger.error("Error saving feedback", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Failed to submit feedback: " + e.getMessage()));
            } else {
                redirect(req, resp, "/patient/appointments?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        }
    }
}
