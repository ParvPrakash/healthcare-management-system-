package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Feedback;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FeedbackDao extends Repository<Feedback, Integer> {
    List<Feedback> findByDoctorId(int doctorId) throws DatabaseException;
    Map<Integer, Integer> getRatingDistribution(int doctorId) throws DatabaseException;
    double getAverageRating(int doctorId) throws DatabaseException;
    Optional<Feedback> findByAppointmentId(int appointmentId) throws DatabaseException;
}
