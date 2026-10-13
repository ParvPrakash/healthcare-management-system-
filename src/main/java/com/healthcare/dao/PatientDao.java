package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Patient;

import java.util.List;

public interface PatientDao extends Repository<Patient, Integer> {
    List<Patient> searchPatients(String keyword, int limit, int offset) throws DatabaseException;
    int countSearchPatients(String keyword) throws DatabaseException;
    void savePatientDetails(Patient patient) throws DatabaseException;
    void updatePatientDetails(Patient patient) throws DatabaseException;
}
