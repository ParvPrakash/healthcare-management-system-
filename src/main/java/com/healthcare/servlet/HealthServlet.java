package com.healthcare.servlet;

import com.healthcare.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

/**
 * Health check endpoint for tunnel readiness checks and monitoring tools.
 * Responds at /health.
 */
@WebServlet(name = "HealthServlet", urlPatterns = {"/health"})
public class HealthServlet extends HttpServlet {
    private static final long START_TIME = System.currentTimeMillis();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        boolean dbOk = DBConnection.isHealthy();
        long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;

        PrintWriter out = resp.getWriter();
        if (dbOk) {
            resp.setStatus(HttpServletResponse.SC_OK);
            out.printf("{\"status\":\"UP\",\"database\":\"OK\",\"uptimeSeconds\":%d,\"timestamp\":%d}%n",
                    uptimeSeconds, System.currentTimeMillis());
        } else {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            out.printf("{\"status\":\"DOWN\",\"database\":\"UNHEALTHY\",\"uptimeSeconds\":%d,\"timestamp\":%d}%n",
                    uptimeSeconds, System.currentTimeMillis());
        }
        out.flush();
    }
}
