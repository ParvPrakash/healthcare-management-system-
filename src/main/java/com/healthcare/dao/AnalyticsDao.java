package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;

import java.util.Map;

public interface AnalyticsDao {
    Map<String, Integer> getAppointmentsPerDay(int days) throws DatabaseException;
    Map<String, Integer> getAppointmentsByStatus() throws DatabaseException;
    Map<String, Integer> getTopDoctors(int limit) throws DatabaseException;
    Map<String, Integer> getNewUsersTrend(int days) throws DatabaseException;
    Map<String, Integer> getBusiestHours() throws DatabaseException;
    Map<String, Object> getSystemOverviewStats() throws DatabaseException;
}
