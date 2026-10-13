package com.healthcare.hms.dao;

import com.healthcare.hms.model.DiagnosticService;
import com.healthcare.hms.model.DiagnosticTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DiagnosticDAO {
    private final JdbcTemplate jdbcTemplate;

    public DiagnosticDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<DiagnosticService> serviceRowMapper = (rs, rowNum) -> {
        DiagnosticService s = new DiagnosticService();
        s.setServiceId(rs.getInt("service_id"));
        s.setServiceName(rs.getString("service_name"));
        s.setCategory(rs.getString("category"));
        s.setCost(rs.getBigDecimal("cost"));
        s.setTurnaroundHours(rs.getInt("turnaround_hours"));
        s.setDescription(rs.getString("description"));
        return s;
    };

    private final RowMapper<DiagnosticTest> testRowMapper = (rs, rowNum) -> {
        DiagnosticTest t = new DiagnosticTest();
        t.setTestId(rs.getInt("test_id"));
        t.setPatientId(rs.getInt("patient_id"));
        t.setServiceId(rs.getInt("service_id"));
        t.setDoctorId(rs.getObject("doctor_id") != null ? rs.getInt("doctor_id") : null);
        t.setAppointmentId(rs.getObject("appointment_id") != null ? rs.getInt("appointment_id") : null);
        Date d = rs.getDate("booking_date");
        if (d != null) t.setBookingDate(d.toLocalDate());
        t.setStatus(rs.getString("status"));
        t.setResults(rs.getString("results"));
        t.setNormalRange(rs.getString("normal_range"));
        t.setRemarks(rs.getString("remarks"));
        Timestamp ca = rs.getTimestamp("completed_at");
        if (ca != null) t.setCompletedAt(ca.toLocalDateTime());

        try {
            t.setPatientName(rs.getString("patient_first_name") + " " + rs.getString("patient_last_name"));
            t.setServiceName(rs.getString("service_name"));
            t.setCategory(rs.getString("category"));
            t.setCost(rs.getBigDecimal("cost"));
            if (rs.getString("doctor_first_name") != null) {
                t.setDoctorName(rs.getString("doctor_first_name") + " " + rs.getString("doctor_last_name"));
            }
        } catch (Exception ignored) {}

        return t;
    };

    public List<DiagnosticService> findAllServices() {
        String sql = "SELECT * FROM diagnostic_services ORDER BY category ASC, service_name ASC";
        return jdbcTemplate.query(sql, serviceRowMapper);
    }

    public Optional<DiagnosticService> findServiceById(Integer serviceId) {
        String sql = "SELECT * FROM diagnostic_services WHERE service_id = ?";
        List<DiagnosticService> list = jdbcTemplate.query(sql, serviceRowMapper, serviceId);
        return list.stream().findFirst();
    }

    public List<DiagnosticTest> findAllTests(Integer patientId, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, " +
            "p.first_name AS patient_first_name, p.last_name AS patient_last_name, " +
            "s.service_name, s.category, s.cost, " +
            "d.first_name AS doctor_first_name, d.last_name AS doctor_last_name " +
            "FROM diagnostic_tests t " +
            "JOIN patients p ON t.patient_id = p.patient_id " +
            "JOIN diagnostic_services s ON t.service_id = s.service_id " +
            "LEFT JOIN doctors d ON t.doctor_id = d.doctor_id " +
            "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (patientId != null) {
            sql.append("AND t.patient_id = ? ");
            params.add(patientId);
        }
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND UPPER(t.status) = UPPER(?) ");
            params.add(status.trim());
        }

        sql.append("ORDER BY t.booking_date DESC, t.test_id DESC");
        return jdbcTemplate.query(sql.toString(), testRowMapper, params.toArray());
    }

    public Optional<DiagnosticTest> findTestById(Integer testId) {
        String sql = "SELECT t.*, " +
                     "p.first_name AS patient_first_name, p.last_name AS patient_last_name, " +
                     "s.service_name, s.category, s.cost, " +
                     "d.first_name AS doctor_first_name, d.last_name AS doctor_last_name " +
                     "FROM diagnostic_tests t " +
                     "JOIN patients p ON t.patient_id = p.patient_id " +
                     "JOIN diagnostic_services s ON t.service_id = s.service_id " +
                     "LEFT JOIN doctors d ON t.doctor_id = d.doctor_id " +
                     "WHERE t.test_id = ?";
        List<DiagnosticTest> list = jdbcTemplate.query(sql, testRowMapper, testId);
        return list.stream().findFirst();
    }

    public DiagnosticTest saveTest(DiagnosticTest test) {
        String sql = "INSERT INTO diagnostic_tests (patient_id, service_id, doctor_id, appointment_id, booking_date, status, normal_range, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, test.getPatientId());
            ps.setInt(2, test.getServiceId());
            if (test.getDoctorId() != null) ps.setInt(3, test.getDoctorId()); else ps.setNull(3, java.sql.Types.INTEGER);
            if (test.getAppointmentId() != null) ps.setInt(4, test.getAppointmentId()); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setDate(5, Date.valueOf(test.getBookingDate()));
            ps.setString(6, test.getStatus() != null ? test.getStatus() : "ORDERED");
            ps.setString(7, test.getNormalRange());
            ps.setString(8, test.getRemarks());
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    test.setTestId(n.intValue());
                    break;
                }
            }
        }
        return test;
    }

    public boolean updateTestResults(Integer testId, String status, String results, String normalRange, String remarks) {
        String sql = "UPDATE diagnostic_tests SET status = ?, results = ?, normal_range = COALESCE(?, normal_range), " +
                     "remarks = COALESCE(?, remarks), completed_at = CASE WHEN ? = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE completed_at END " +
                     "WHERE test_id = ?";
        return jdbcTemplate.update(sql, status, results, normalRange, remarks, status, testId) > 0;
    }
}
