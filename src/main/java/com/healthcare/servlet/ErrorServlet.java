package com.healthcare.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling HTTP errors, 404 Not Found, and uncaught 500 exceptions.
 */
@WebServlet(name = "ErrorServlet", urlPatterns = {"/error", "/error/404", "/error/500"})
public class ErrorServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        Integer statusCode = (Integer) req.getAttribute("jakarta.servlet.error.status_code");
        String message = (String) req.getAttribute("jakarta.servlet.error.message");
        Throwable throwable = (Throwable) req.getAttribute("jakarta.servlet.error.exception");

        if (statusCode == null) {
            statusCode = "/error/404".equals(path) ? 404 : 500;
        }

        req.setAttribute("statusCode", statusCode);
        req.setAttribute("errorMessage", message != null ? message : "An unexpected error occurred");
        if (throwable != null) {
            req.setAttribute("exceptionType", throwable.getClass().getName());
        }

        if (statusCode == 404 || "/error/404".equals(path)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            forwardToView(req, resp, "404");
        } else {
            resp.setStatus(statusCode);
            forwardToView(req, resp, "500");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
