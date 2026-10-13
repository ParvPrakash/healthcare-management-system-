package com.healthcare.servlet;

import com.healthcare.dao.ScheduleDao;
import com.healthcare.dao.ScheduleDaoImpl;
import com.healthcare.model.Schedule;
import com.healthcare.model.TimeSlot;
import com.healthcare.model.User;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for Doctor scheduling, recurring templates, and dynamic time slot generation.
 */
@WebServlet(name = "ScheduleServlet", urlPatterns = {"/doctor/schedule", "/doctor/schedule/save", "/doctor/schedule/generate", "/api/slots"})
public class ScheduleServlet extends BaseServlet {
    private final ScheduleDao scheduleDao = new ScheduleDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        // AJAX API endpoint for slot picker
        if ("/api/slots".equals(path)) {
            handleGetSlotsApi(req, resp);
            return;
        }

        User doctor = getAuthenticatedUser(req);
        if (doctor == null) {
            redirect(req, resp, "/login");
            return;
        }

        try {
            List<Schedule> schedules = scheduleDao.findByDoctorId(doctor.getId());
            LocalDate today = LocalDate.now();
            List<TimeSlot> todaySlots = scheduleDao.findSlotsByDoctorAndDate(doctor.getId(), today);

            req.setAttribute("schedules", schedules);
            req.setAttribute("todaySlots", todaySlots);
            req.setAttribute("todayDate", today.toString());

        } catch (Exception e) {
            logger.error("Error retrieving doctor schedule", e);
            req.setAttribute("errorMessage", "Error loading schedule: " + e.getMessage());
        }

        forwardToView(req, resp, "doctor/schedule");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;

        User doctor = getAuthenticatedUser(req);
        if (doctor == null) {
            redirect(req, resp, "/login");
            return;
        }

        String path = req.getServletPath();

        if ("/doctor/schedule/save".equals(path)) {
            handleSaveSchedule(req, resp, doctor);
        } else if ("/doctor/schedule/generate".equals(path)) {
            handleGenerateSlots(req, resp, doctor);
        }
    }

    private void handleGetSlotsApi(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String doctorIdStr = req.getParameter("doctorId");
        String dateStr = req.getParameter("date");

        if (doctorIdStr == null || dateStr == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("doctorId and date are required"));
            return;
        }

        try {
            int doctorId = Integer.parseInt(doctorIdStr);
            LocalDate date = LocalDate.parse(dateStr);

            List<TimeSlot> slots = scheduleDao.findSlotsByDoctorAndDate(doctorId, date);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Slots loaded", slots));

        } catch (Exception e) {
            logger.error("Error fetching slots for doctor on date", e);
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiResponse.error("Failed to load time slots"));
        }
    }

    private void handleSaveSchedule(HttpServletRequest req, HttpServletResponse resp, User doctor)
            throws ServletException, IOException {
        String day = req.getParameter("dayOfWeek");
        String startStr = req.getParameter("startTime");
        String endStr = req.getParameter("endTime");
        String durationStr = req.getParameter("slotDurationMinutes");

        try {
            Schedule schedule = new Schedule();
            schedule.setDoctorId(doctor.getId());
            schedule.setDayOfWeek(day);
            schedule.setStartTime(LocalTime.parse(startStr));
            schedule.setEndTime(LocalTime.parse(endStr));
            schedule.setSlotDurationMinutes(durationStr != null ? Integer.parseInt(durationStr) : 30);
            schedule.setActive(true);

            scheduleDao.save(schedule);
            // Automatically generate slots for this doctor for next 14 days
            scheduleDao.generateSlotsForDoctor(doctor.getId(), LocalDate.now(), 14);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Schedule added and slots generated"));
            } else {
                redirect(req, resp, "/doctor/schedule?success=added");
            }
        } catch (Exception e) {
            logger.error("Failed to save schedule", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Failed to save schedule: " + e.getMessage()));
            } else {
                req.setAttribute("errorMessage", "Error saving schedule: " + e.getMessage());
                doGet(req, resp);
            }
        }
    }

    private void handleGenerateSlots(HttpServletRequest req, HttpServletResponse resp, User doctor)
            throws IOException {
        try {
            scheduleDao.generateSlotsForDoctor(doctor.getId(), LocalDate.now(), 14);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Slots generated for the next 14 days"));
            } else {
                redirect(req, resp, "/doctor/schedule?success=slots_generated");
            }
        } catch (Exception e) {
            logger.error("Error generating slots", e);
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ApiResponse.error("Failed to generate slots"));
        }
    }
}
