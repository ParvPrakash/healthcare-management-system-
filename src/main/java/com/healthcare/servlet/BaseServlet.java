package com.healthcare.servlet;

import com.google.gson.Gson;
import com.healthcare.model.User;
import com.healthcare.util.ApiResponse;
import com.healthcare.util.CsrfUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base abstract controller demonstrating the complete Jakarta Servlet lifecycle:
 * init(), service(), doGet(), doPost(), doPut(), doDelete(), and destroy() with logging.
 */
public abstract class BaseServlet extends HttpServlet {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected static final Gson gson = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class,
                    (com.google.gson.JsonSerializer<java.time.LocalDate>) (src, typeOfSrc, context) ->
                            new com.google.gson.JsonPrimitive(src != null ? src.toString() : ""))
            .registerTypeAdapter(java.time.LocalDate.class,
                    (com.google.gson.JsonDeserializer<java.time.LocalDate>) (json, typeOfT, context) ->
                            java.time.LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(java.time.LocalTime.class,
                    (com.google.gson.JsonSerializer<java.time.LocalTime>) (src, typeOfSrc, context) ->
                            new com.google.gson.JsonPrimitive(src != null ? src.toString() : ""))
            .registerTypeAdapter(java.time.LocalTime.class,
                    (com.google.gson.JsonDeserializer<java.time.LocalTime>) (json, typeOfT, context) ->
                            java.time.LocalTime.parse(json.getAsString()))
            .create();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        logger.info("[LIFECYCLE: init] Servlet initialized: {}", getClass().getSimpleName());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("[LIFECYCLE: service] Dispatching {} {}", req.getMethod(), req.getRequestURI());
        super.service(req, resp);
    }

    @Override
    public void destroy() {
        logger.info("[LIFECYCLE: destroy] Servlet destroyed: {}", getClass().getSimpleName());
        super.destroy();
    }

    /**
     * Forwards request to a JSP view under WEB-INF/views/.
     */
    protected void forwardToView(HttpServletRequest req, HttpServletResponse resp, String viewName)
            throws ServletException, IOException {
        String viewPath = "/WEB-INF/views/" + viewName + ".jsp";
        // Refresh CSRF token for the view forms
        req.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(req.getSession(true)));
        req.getRequestDispatcher(viewPath).forward(req, resp);
    }

    /**
     * Sends an HTTP redirect with optional status feedback query param.
     */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /**
     * Serializes an object to JSON and sends it with specified HTTP status code.
     */
    protected <T> void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse<T> responseObj)
            throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(responseObj));
        out.flush();
    }

    /**
     * Validates CSRF token for state-changing POST/PUT/DELETE requests.
     */
    protected boolean validateCsrf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!CsrfUtil.isValidToken(req)) {
            logger.warn("CSRF validation failed for request: {}", req.getRequestURI());
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN, ApiResponse.error("Invalid security token (CSRF). Please refresh the page."));
            } else {
                req.setAttribute("errorMessage", "Security validation failed. Please try again.");
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
            }
            return false;
        }
        return true;
    }

    /**
     * Detects if request is an asynchronous fetch/AJAX request.
     */
    protected boolean isAjaxRequest(HttpServletRequest req) {
        String header = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(header) || (accept != null && accept.contains("application/json"));
    }

    /**
     * Retrieves currently authenticated user from session.
     */
    protected User getAuthenticatedUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("USER") : null;
    }

    /**
     * Resolves client IP address considering proxy headers.
     */
    protected String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
