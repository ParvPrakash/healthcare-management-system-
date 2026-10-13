package com.healthcare.servlet;

import com.healthcare.dao.DoctorDao;
import com.healthcare.dao.DoctorDaoImpl;
import com.healthcare.model.Doctor;
import com.healthcare.service.DoctorCache;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for Doctor search, specialization filtering, and LRU cached profile views.
 */
@WebServlet(name = "DoctorServlet", urlPatterns = {"/patient/doctors", "/patient/booking", "/api/doctors", "/api/specializations"})
public class DoctorServlet extends BaseServlet {
    private final DoctorDao doctorDao = new DoctorDaoImpl();
    // LRU Cache for top accessed doctors (§4.2)
    private final DoctorCache doctorCache = new DoctorCache(25);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/api/specializations".equals(path)) {
            try {
                List<String> specs = doctorDao.findAllSpecializations();
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Specializations retrieved", specs));
            } catch (Exception e) {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiResponse.error("Failed to load specializations"));
            }
            return;
        }

        String keyword = req.getParameter("q");
        String spec = req.getParameter("spec");
        String ratingStr = req.getParameter("rating");
        Double minRating = null;
        if (ratingStr != null && !ratingStr.isEmpty()) {
            try {
                minRating = Double.parseDouble(ratingStr);
            } catch (NumberFormatException ignored) {}
        }

        try {
            List<Doctor> doctors = doctorDao.searchDoctors(keyword, spec, minRating);
            List<String> specializations = doctorDao.findAllSpecializations();

            // Populate LRU cache
            for (Doctor d : doctors) {
                doctorCache.put(d);
            }

            if (isAjaxRequest(req) || "/api/doctors".equals(path)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Doctors retrieved", doctors));
                return;
            }

            req.setAttribute("doctors", doctors);
            req.setAttribute("specializations", specializations);
            req.setAttribute("selectedSpec", spec != null ? spec : "ALL");
            req.setAttribute("keyword", keyword != null ? keyword : "");
            req.setAttribute("minRating", minRating != null ? minRating : 0.0);

            forwardToView(req, resp, "patient/booking");

        } catch (Exception e) {
            logger.error("Error retrieving doctor directory", e);
            req.setAttribute("errorMessage", "Error loading doctors: " + e.getMessage());
            forwardToView(req, resp, "patient/booking");
        }
    }
}
