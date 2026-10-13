package com.healthcare.hms.service;

import com.healthcare.hms.dao.AuditLogDAO;
import com.healthcare.hms.dao.PatientDAO;
import com.healthcare.hms.model.AuditLog;
import com.healthcare.hms.model.Patient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PatientService {
    private final PatientDAO patientDAO;
    private final AuditLogDAO auditLogDAO;

    public PatientService(PatientDAO patientDAO, AuditLogDAO auditLogDAO) {
        this.patientDAO = patientDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<Patient> getAllPatients(String search) {
        return patientDAO.findAll(search);
    }

    public Optional<Patient> getPatientById(Integer patientId) {
        return patientDAO.findById(patientId);
    }

    @Transactional
    public Patient registerPatient(Patient patient, String user) {
        Patient saved = patientDAO.save(patient);

        // Audit Trail entry
        AuditLog log = new AuditLog();
        log.setEntityType("PATIENT");
        log.setEntityId(saved.getPatientId());
        log.setActionType("CREATE");
        log.setFieldName("PATIENT_REGISTRATION");
        log.setOldValue(null);
        log.setNewValue(saved.getFullName() + " (" + saved.getPhone() + ")");
        log.setModifiedBy(user != null ? user : "Healthcare Staff");
        log.setReasonOrNotes("Structured patient intake registration completed.");
        auditLogDAO.save(log);

        return saved;
    }

    @Transactional
    public boolean updatePatient(Patient updatedPatient, String user, String reason) {
        Optional<Patient> existingOpt = patientDAO.findById(updatedPatient.getPatientId());
        if (existingOpt.isEmpty()) {
            return false;
        }

        Patient old = existingOpt.get();
        String actor = (user != null && !user.trim().isEmpty()) ? user : "Healthcare Staff";
        String note = (reason != null && !reason.trim().isEmpty()) ? reason : "Patient profile update";

        // Trace field changes for Edit-History
        logDiff(updatedPatient.getPatientId(), "phone", old.getPhone(), updatedPatient.getPhone(), actor, note);
        logDiff(updatedPatient.getPatientId(), "email", old.getEmail(), updatedPatient.getEmail(), actor, note);
        logDiff(updatedPatient.getPatientId(), "address", old.getAddress(), updatedPatient.getAddress(), actor, note);
        logDiff(updatedPatient.getPatientId(), "emergencyContactName", old.getEmergencyContactName(), updatedPatient.getEmergencyContactName(), actor, note);
        logDiff(updatedPatient.getPatientId(), "emergencyContactPhone", old.getEmergencyContactPhone(), updatedPatient.getEmergencyContactPhone(), actor, note);
        logDiff(updatedPatient.getPatientId(), "allergies", old.getAllergies(), updatedPatient.getAllergies(), actor, note);
        logDiff(updatedPatient.getPatientId(), "medicalHistory", old.getMedicalHistory(), updatedPatient.getMedicalHistory(), actor, note);

        return patientDAO.update(updatedPatient);
    }

    private void logDiff(Integer patientId, String field, String oldVal, String newVal, String actor, String note) {
        String o = oldVal == null ? "" : oldVal.trim();
        String n = newVal == null ? "" : newVal.trim();
        if (!Objects.equals(o, n)) {
            AuditLog log = new AuditLog();
            log.setEntityType("PATIENT");
            log.setEntityId(patientId);
            log.setActionType("UPDATE");
            log.setFieldName(field);
            log.setOldValue(o.isEmpty() ? "(empty)" : o);
            log.setNewValue(n.isEmpty() ? "(empty)" : n);
            log.setModifiedBy(actor);
            log.setReasonOrNotes(note);
            auditLogDAO.save(log);
        }
    }

    @Transactional
    public boolean deletePatient(Integer patientId, String user) {
        AuditLog log = new AuditLog();
        log.setEntityType("PATIENT");
        log.setEntityId(patientId);
        log.setActionType("DELETE");
        log.setFieldName("PATIENT_RECORD");
        log.setOldValue("Patient ID #" + patientId);
        log.setNewValue("REMOVED");
        log.setModifiedBy(user != null ? user : "Healthcare Administrator");
        log.setReasonOrNotes("Patient record archived/deleted from the system.");
        auditLogDAO.save(log);

        return patientDAO.delete(patientId);
    }
}
