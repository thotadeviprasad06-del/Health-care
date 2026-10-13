package com.healthcare.hms.model;

import java.time.LocalDateTime;

public class AuditLog {
    private Integer logId;
    private String entityType;
    private Integer entityId;
    private String actionType;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String modifiedBy;
    private LocalDateTime timestamp;
    private String reasonOrNotes;

    public AuditLog() {}

    public AuditLog(Integer logId, String entityType, Integer entityId, String actionType,
                    String fieldName, String oldValue, String newValue,
                    String modifiedBy, String reasonOrNotes) {
        this.logId = logId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actionType = actionType;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.modifiedBy = modifiedBy;
        this.reasonOrNotes = reasonOrNotes;
    }

    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getReasonOrNotes() { return reasonOrNotes; }
    public void setReasonOrNotes(String reasonOrNotes) { this.reasonOrNotes = reasonOrNotes; }
}
