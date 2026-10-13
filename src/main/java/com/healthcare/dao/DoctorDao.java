package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Doctor;
import com.healthcare.util.Pair;

import java.util.List;
import java.util.Optional;

public interface DoctorDao extends Repository<Doctor, Integer> {
    List<Doctor> findBySpecialization(String specialization) throws DatabaseException;
    List<String> findAllSpecializations() throws DatabaseException;
    List<Doctor> searchDoctors(String keyword, String specialization, Double minRating) throws DatabaseException;
    void saveDoctorDetails(Doctor doctor) throws DatabaseException;
    void updateDoctorDetails(Doctor doctor) throws DatabaseException;
    Pair<Double, Integer> getDoctorRatingStats(int doctorId) throws DatabaseException;
}
