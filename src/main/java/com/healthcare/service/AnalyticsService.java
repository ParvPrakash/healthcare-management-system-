package com.healthcare.service;

import com.google.gson.Gson;
import com.healthcare.dao.AnalyticsDao;
import com.healthcare.dao.AnalyticsDaoImpl;
import com.healthcare.dao.DoctorDao;
import com.healthcare.dao.DoctorDaoImpl;
import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service orchestrating administrative business intelligence and KPI charts.
 * Demonstrates Streams API with Collectors.averagingDouble (§4.2).
 */
public class AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private static final Gson gson = new Gson();

    private final AnalyticsDao analyticsDao;
    private final DoctorDao doctorDao;

    // In-memory cache for fast repeated dashboard hits
    private final Map<String, Object> cachedOverview = new ConcurrentHashMap<>();

    public AnalyticsService() {
        this.analyticsDao = new AnalyticsDaoImpl();
        this.doctorDao = new DoctorDaoImpl();
    }

    public AnalyticsService(AnalyticsDao analyticsDao, DoctorDao doctorDao) {
        this.analyticsDao = analyticsDao;
        this.doctorDao = doctorDao;
    }

    /**
     * Refreshes background cached analytics.
     */
    public void refreshCache() {
        try {
            Map<String, Object> fresh = analyticsDao.getSystemOverviewStats();
            cachedOverview.clear();
            cachedOverview.putAll(fresh);
            logger.debug("Analytics cache refreshed successfully.");
        } catch (DatabaseException e) {
            logger.error("Failed to refresh analytics cache", e);
        }
    }

    public Map<String, Object> getOverviewStats() throws DatabaseException {
        if (cachedOverview.isEmpty()) {
            refreshCache();
        }
        return Collections.unmodifiableMap(cachedOverview);
    }

    /**
     * Demonstrates Streams and Collectors.averagingDouble on doctor consultation fees.
     */
    public double calculateAverageDoctorFee() throws DatabaseException {
        List<Doctor> doctors = doctorDao.findAll();
        if (doctors.isEmpty()) return 0.0;
        return doctors.stream()
                .filter(d -> d.getConsultationFee() != null)
                .collect(Collectors.averagingDouble(d -> d.getConsultationFee().doubleValue()));
    }

    /**
     * Produces formatted JSON string consumed by Chart.js.
     */
    public String getChartDataJson(int days) throws DatabaseException {
        Map<String, Object> chartPayload = new HashMap<>();

        // 1. Appointments per day (Area/Line chart)
        Map<String, Integer> perDay = analyticsDao.getAppointmentsPerDay(days);
        chartPayload.put("appointmentsPerDay", perDay);

        // 2. Status distribution (Doughnut chart)
        Map<String, Integer> byStatus = analyticsDao.getAppointmentsByStatus();
        chartPayload.put("appointmentsByStatus", byStatus);

        // 3. Top doctors (Bar chart)
        Map<String, Integer> topDoctors = analyticsDao.getTopDoctors(5);
        chartPayload.put("topDoctors", topDoctors);

        // 4. New user trend (Line chart)
        Map<String, Integer> userTrend = analyticsDao.getNewUsersTrend(days);
        chartPayload.put("userTrend", userTrend);

        // 5. Busiest hours (Bar chart)
        Map<String, Integer> busiestHours = analyticsDao.getBusiestHours();
        chartPayload.put("busiestHours", busiestHours);

        return gson.toJson(chartPayload);
    }
}
