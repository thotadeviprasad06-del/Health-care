package com.healthcare.hms.service;

import com.healthcare.hms.dao.AuditLogDAO;
import com.healthcare.hms.dao.MedicalRecordDAO;
import com.healthcare.hms.model.AuditLog;
import com.healthcare.hms.model.MedicalRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedicalRecordService {
    private final MedicalRecordDAO medicalRecordDAO;
    private final AuditLogDAO auditLogDAO;

    public MedicalRecordService(MedicalRecordDAO medicalRecordDAO, AuditLogDAO auditLogDAO) {
        this.medicalRecordDAO = medicalRecordDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<MedicalRecord> getAllRecords() {
        return medicalRecordDAO.findAll();
    }

    public List<MedicalRecord> getRecordsByPatientId(Integer patientId) {
        return medicalRecordDAO.findByPatientId(patientId);
    }

    public Optional<MedicalRecord> getRecordById(Integer recordId) {
        return medicalRecordDAO.findById(recordId);
    }

    @Transactional
    public MedicalRecord createRecord(MedicalRecord record, String user) {
        MedicalRecord saved = medicalRecordDAO.save(record);

        AuditLog log = new AuditLog();
        log.setEntityType("MEDICAL_RECORD");
        log.setEntityId(saved.getRecordId());
        log.setActionType("CREATE");
        log.setFieldName("DIAGNOSIS");
        log.setOldValue(null);
        log.setNewValue(saved.getDiagnosis());
        log.setModifiedBy(user != null ? user : "Attending Physician");
        log.setReasonOrNotes("Clinical encounter documented for Patient ID #" + saved.getPatientId());
        auditLogDAO.save(log);

        return saved;
    }
}
