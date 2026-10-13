package com.healthcare.servlet;

import com.healthcare.model.MedicalRecord;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.service.RecordService;
import com.healthcare.service.UserService;
import com.healthcare.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Electronic Medical Records (EMR).
 */
@WebServlet(name = "MedicalRecordServlet", urlPatterns = {"/doctor/records", "/patient/records", "/records/save"})
public class MedicalRecordServlet extends BaseServlet {
    private final RecordService recordService = new RecordService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            redirect(req, resp, "/login");
            return;
        }

        try {
            if (user.getRole() == Role.PATIENT) {
                List<MedicalRecord> history = recordService.getPatientHistory(user.getId());
                req.setAttribute("records", history);
                forwardToView(req, resp, "patient/records");

            } else if (user.getRole() == Role.DOCTOR) {
                String patientIdStr = req.getParameter("patientId");
                if (patientIdStr != null && !patientIdStr.isEmpty()) {
                    int patientId = Integer.parseInt(patientIdStr);
                    Patient patient = userService.getPatientById(patientId).orElse(null);
                    List<MedicalRecord> patientRecords = recordService.getPatientHistory(patientId);
                    req.setAttribute("selectedPatient", patient);
                    req.setAttribute("patientRecords", patientRecords);
                }

                List<MedicalRecord> recentRecords = recordService.getDoctorRecords(user.getId());
                req.setAttribute("records", recentRecords);
                forwardToView(req, resp, "doctor/records");
            }
        } catch (Exception e) {
            logger.error("Error retrieving medical records", e);
            req.setAttribute("errorMessage", "Error loading medical records: " + e.getMessage());
            forwardToView(req, resp, "doctor/records");
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

        String ip = getClientIp(req);
        String recIdStr = req.getParameter("id");
        String appIdStr = req.getParameter("appointmentId");
        String patientIdStr = req.getParameter("patientId");
        String diagnosis = req.getParameter("diagnosis");
        String prescription = req.getParameter("prescription");
        String bp = req.getParameter("vitalsBloodPressure");
        String hr = req.getParameter("vitalsHeartRate");
        String temp = req.getParameter("vitalsTemperature");
        String notes = req.getParameter("notes");

        try {
            MedicalRecord record = new MedicalRecord();
            if (recIdStr != null && !recIdStr.trim().isEmpty()) {
                record.setId(Integer.parseInt(recIdStr));
            }
            if (appIdStr != null && !appIdStr.trim().isEmpty()) {
                record.setAppointmentId(Integer.parseInt(appIdStr));
            }
            record.setPatientId(Integer.parseInt(patientIdStr));
            record.setDoctorId(user.getId());
            record.setDiagnosis(diagnosis);
            record.setPrescription(prescription);
            record.setVitalsBloodPressure(bp);
            record.setVitalsHeartRate(hr);
            record.setVitalsTemperature(temp);
            record.setNotes(notes);
            record.setRecordDate(LocalDate.now());

            recordService.createOrUpdateRecord(record, user.getId(), ip);

            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Medical record saved successfully!"));
            } else {
                redirect(req, resp, "/doctor/records?patientId=" + patientIdStr + "&success=saved");
            }
        } catch (Exception e) {
            logger.error("Error saving medical record", e);
            if (isAjaxRequest(req)) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error(e.getMessage()));
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                doGet(req, resp);
            }
        }
    }
}
