package com.healthcare.hms.service;

import com.healthcare.hms.dao.AuditLogDAO;
import com.healthcare.hms.dao.DiagnosticDAO;
import com.healthcare.hms.model.AuditLog;
import com.healthcare.hms.model.DiagnosticService;
import com.healthcare.hms.model.DiagnosticTest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LabDiagnosticService {
    private final DiagnosticDAO diagnosticDAO;
    private final AuditLogDAO auditLogDAO;

    public LabDiagnosticService(DiagnosticDAO diagnosticDAO, AuditLogDAO auditLogDAO) {
        this.diagnosticDAO = diagnosticDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<DiagnosticService> getAllCatalogServices() {
        return diagnosticDAO.findAllServices();
    }

    public Optional<DiagnosticService> getCatalogServiceById(Integer serviceId) {
        return diagnosticDAO.findServiceById(serviceId);
    }

    public List<DiagnosticTest> getAllBookedTests(Integer patientId, String status) {
        return diagnosticDAO.findAllTests(patientId, status);
    }

    public Optional<DiagnosticTest> getTestById(Integer testId) {
        return diagnosticDAO.findTestById(testId);
    }

    @Transactional
    public DiagnosticTest orderTest(DiagnosticTest test, String user) {
        DiagnosticTest saved = diagnosticDAO.saveTest(test);

        AuditLog log = new AuditLog();
        log.setEntityType("DIAGNOSTIC_TEST");
        log.setEntityId(saved.getTestId());
        log.setActionType("CREATE");
        log.setFieldName("STATUS");
        log.setOldValue(null);
        log.setNewValue("ORDERED");
        log.setModifiedBy(user != null ? user : "Lab Technician");
        log.setReasonOrNotes("Diagnostic test booked for Patient ID #" + saved.getPatientId() + " (Service #" + saved.getServiceId() + ")");
        auditLogDAO.save(log);

        return saved;
    }

    @Transactional
    public boolean updateTestResults(Integer testId, String status, String results, String normalRange, String remarks, String user) {
        Optional<DiagnosticTest> existingOpt = diagnosticDAO.findTestById(testId);
        if (existingOpt.isEmpty()) return false;

        boolean updated = diagnosticDAO.updateTestResults(testId, status, results, normalRange, remarks);
        if (updated) {
            AuditLog log = new AuditLog();
            log.setEntityType("DIAGNOSTIC_TEST");
            log.setEntityId(testId);
            log.setActionType("UPDATE");
            log.setFieldName("STATUS_RESULTS");
            log.setOldValue(existingOpt.get().getStatus());
            log.setNewValue(status + " | " + (results != null ? results : ""));
            log.setModifiedBy(user != null ? user : "Pathologist");
            log.setReasonOrNotes("Diagnostic test status and findings updated.");
            auditLogDAO.save(log);
        }
        return updated;
    }
}
