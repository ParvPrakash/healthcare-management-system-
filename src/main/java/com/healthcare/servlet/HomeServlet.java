package com.healthcare.servlet;

import com.healthcare.dao.DoctorDaoImpl;
import com.healthcare.model.Doctor;
import com.healthcare.model.User;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the public home/landing page.
 * Demonstrates ServletConfig init-params (§4.7).
 */
@WebServlet(
        name = "HomeServlet",
        urlPatterns = {"", "/home"},
        initParams = {
                @WebInitParam(name = "defaultTagline", value = "Compassionate care, effortless scheduling"),
                @WebInitParam(name = "featuredBanner", value = "Welcome to CareFirst Healthcare Center")
        }
)
public class HomeServlet extends BaseServlet {
    private String defaultTagline;
    private String featuredBanner;
    private final DoctorDaoImpl doctorDao = new DoctorDaoImpl();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Demonstrating ServletConfig init-param retrieval
        this.defaultTagline = config.getInitParameter("defaultTagline");
        this.featuredBanner = config.getInitParameter("featuredBanner");
        logger.info("HomeServlet initialized with banner: '{}'", featuredBanner);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user != null) {
            req.setAttribute("currentUser", user);
        }

        try {
            List<Doctor> featuredDoctors = doctorDao.searchDoctors(null, null, 4.0);
            req.setAttribute("featuredDoctors", featuredDoctors);
            req.setAttribute("tagline", defaultTagline);
            req.setAttribute("banner", featuredBanner);
        } catch (Exception e) {
            logger.error("Error loading doctors for landing page", e);
        }

        forwardToView(req, resp, "landing");
    }
}
