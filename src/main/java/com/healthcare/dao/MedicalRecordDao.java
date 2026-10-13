package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.MedicalRecord;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordDao extends Repository<MedicalRecord, Integer> {
    List<MedicalRecord> findByPatientId(int patientId) throws DatabaseException;
    List<MedicalRecord> findByDoctorId(int doctorId) throws DatabaseException;
    Optional<MedicalRecord> findByAppointmentId(int appointmentId) throws DatabaseException;
}
