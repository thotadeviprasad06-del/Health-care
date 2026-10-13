package com.healthcare.hms.service;

import com.healthcare.hms.dao.AuditLogDAO;
import com.healthcare.hms.model.AuditLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogDAO auditLogDAO;

    public AuditLogService(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDAO.findAll(limit > 0 ? limit : 50);
    }

    public List<AuditLog> getEntityHistory(String entityType, Integer entityId) {
        return auditLogDAO.findByEntity(entityType, entityId);
    }
}
