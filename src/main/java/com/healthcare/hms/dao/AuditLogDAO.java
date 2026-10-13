package com.healthcare.hms.dao;

import com.healthcare.hms.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
public class AuditLogDAO {
    private final JdbcTemplate jdbcTemplate;

    public AuditLogDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AuditLog> rowMapper = (rs, rowNum) -> {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("log_id"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getInt("entity_id"));
        log.setActionType(rs.getString("action_type"));
        log.setFieldName(rs.getString("field_name"));
        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));
        log.setModifiedBy(rs.getString("modified_by"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) log.setTimestamp(ts.toLocalDateTime());
        log.setReasonOrNotes(rs.getString("reason_or_notes"));
        return log;
    };

    public List<AuditLog> findAll(int limit) {
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC, log_id DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public List<AuditLog> findByEntity(String entityType, Integer entityId) {
        String sql = "SELECT * FROM audit_logs WHERE UPPER(entity_type) = UPPER(?) AND entity_id = ? ORDER BY timestamp DESC, log_id DESC";
        return jdbcTemplate.query(sql, rowMapper, entityType, entityId);
    }

    public AuditLog save(AuditLog log) {
        String sql = "INSERT INTO audit_logs (entity_type, entity_id, action_type, field_name, old_value, new_value, modified_by, timestamp, reason_or_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "log_id" });
            ps.setString(1, log.getEntityType());
            ps.setInt(2, log.getEntityId());
            ps.setString(3, log.getActionType());
            ps.setString(4, log.getFieldName());
            ps.setString(5, log.getOldValue());
            ps.setString(6, log.getNewValue());
            ps.setString(7, log.getModifiedBy() != null ? log.getModifiedBy() : "Healthcare Staff");
            ps.setString(8, log.getReasonOrNotes());
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    log.setLogId(n.intValue());
                    break;
                }
            }
        }
        return log;
    }
}
