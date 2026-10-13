package com.healthcare.servlet;

import com.healthcare.service.AnalyticsService;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for Performance Analytics, executive KPIs, and Chart.js JSON feeds.
 */
@WebServlet(name = "AnalyticsServlet", urlPatterns = {"/admin/analytics", "/admin/analytics/data"})
public class AnalyticsServlet extends BaseServlet {
    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String daysStr = req.getParameter("days");
        int days = 14;
        if (daysStr != null && !daysStr.isEmpty()) {
            try {
                days = Integer.parseInt(daysStr);
            } catch (NumberFormatException ignored) {}
        }

        try {
            String chartJson = analyticsService.getChartDataJson(days);
            Map<String, Object> stats = analyticsService.getOverviewStats();
            double avgFee = analyticsService.calculateAverageDoctorFee();

            if ("/admin/analytics/data".equals(path) || isAjaxRequest(req)) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(chartJson);
                resp.getWriter().flush();
                return;
            }

            req.setAttribute("chartData", chartJson);
            req.setAttribute("overviewStats", stats);
            req.setAttribute("avgDoctorFee", Math.round(avgFee * 100.0) / 100.0);
            req.setAttribute("selectedDays", days);

            forwardToView(req, resp, "admin/analytics");

        } catch (Exception e) {
            logger.error("Error generating analytics data", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiResponse.error("Analytics failure: " + e.getMessage()));
            } else {
                req.setAttribute("errorMessage", "Error loading analytics: " + e.getMessage());
                forwardToView(req, resp, "admin/analytics");
            }
        }
    }
}
