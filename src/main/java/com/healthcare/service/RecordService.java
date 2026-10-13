package com.healthcare.service;

import com.healthcare.dao.AuditDao;
import com.healthcare.dao.AuditDaoImpl;
import com.healthcare.dao.MedicalRecordDao;
import com.healthcare.dao.MedicalRecordDaoImpl;
import com.healthcare.exception.DatabaseException;
import com.healthcare.exception.ValidationException;
import com.healthcare.model.MedicalRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service managing Electronic Medical Records (EMR).
 */
public class RecordService {
    private static final Logger logger = LoggerFactory.getLogger(RecordService.class);

    private final MedicalRecordDao recordDao;
    private final AuditDao auditDao;

    public RecordService() {
        this.recordDao = new MedicalRecordDaoImpl();
        this.auditDao = new AuditDaoImpl();
    }

    public RecordService(MedicalRecordDao recordDao, AuditDao auditDao) {
        this.recordDao = recordDao;
        this.auditDao = auditDao;
    }

    public MedicalRecord createOrUpdateRecord(MedicalRecord record, Integer doctorId, String ipAddress)
            throws ValidationException, DatabaseException {
        if (record.getDiagnosis() == null || record.getDiagnosis().trim().isEmpty()) {
            throw new ValidationException("Diagnosis is required for clinical record");
        }
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }

        if (record.getId() != null && record.getId() > 0) {
            recordDao.update(record);
            auditDao.logAction(doctorId, "UPDATE_MEDICAL_RECORD", "Updated medical record #" + record.getId(), ipAddress);
            return record;
        } else {
            MedicalRecord saved = recordDao.save(record);
            auditDao.logAction(doctorId, "CREATE_MEDICAL_RECORD", "Created medical record for patient #" + record.getPatientId(), ipAddress);
            return saved;
        }
    }

    public List<MedicalRecord> getPatientHistory(int patientId) throws DatabaseException {
        return recordDao.findByPatientId(patientId);
    }

    public List<MedicalRecord> getDoctorRecords(int doctorId) throws DatabaseException {
        return recordDao.findByDoctorId(doctorId);
    }

    public Optional<MedicalRecord> getRecordById(int recordId) throws DatabaseException {
        return recordDao.findById(recordId);
    }

    public Optional<MedicalRecord> getRecordByAppointmentId(int appointmentId) throws DatabaseException {
        return recordDao.findByAppointmentId(appointmentId);
    }
}
