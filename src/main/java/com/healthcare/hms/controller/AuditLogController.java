package com.healthcare.hms.controller;

import com.healthcare.hms.model.AuditLog;
import com.healthcare.hms.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLog> getRecentLogs(@RequestParam(required = false, defaultValue = "50") int limit) {
        return auditLogService.getRecentLogs(limit);
    }

    @GetMapping("/{entityType}/{entityId}")
    public List<AuditLog> getEntityHistory(@PathVariable String entityType, @PathVariable Integer entityId) {
        return auditLogService.getEntityHistory(entityType, entityId);
    }
}
