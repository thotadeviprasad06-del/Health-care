package com.healthcare.hms.dao;

import com.healthcare.hms.model.DashboardStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
public class AnalyticsDAO {
    private final JdbcTemplate jdbcTemplate;
    private final AppointmentDAO appointmentDAO;
    private final AuditLogDAO auditLogDAO;

    public AnalyticsDAO(JdbcTemplate jdbcTemplate, AppointmentDAO appointmentDAO, AuditLogDAO auditLogDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.appointmentDAO = appointmentDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // 1. Total Patients
        Integer patients = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM patients", Integer.class);
        stats.setTotalPatients(patients != null ? patients : 0);

        // 2. Total Doctors
        Integer doctors = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM doctors WHERE status = 'ACTIVE'", Integer.class);
        stats.setTotalDoctors(doctors != null ? doctors : 0);

        // 3. Today's Appointments
        Integer todayAppts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE", Integer.class);
        stats.setTodayAppointments(todayAppts != null ? todayAppts : 0);

        // 4. Pending Lab Tests
        Integer pendingTests = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM diagnostic_tests WHERE status IN ('ORDERED', 'SAMPLE_COLLECTED', 'IN_PROGRESS')", Integer.class);
        stats.setPendingLabTests(pendingTests != null ? pendingTests : 0);

        // 5. Total Paid Revenue
        BigDecimal paidRev = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(paid_amount), 0) FROM invoices", BigDecimal.class);
        stats.setTotalRevenue(paidRev != null ? paidRev : BigDecimal.ZERO);

        // 6. Pending Revenue
        BigDecimal pendingRev = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(total_amount - paid_amount), 0) FROM invoices WHERE payment_status <> 'PAID'", BigDecimal.class);
        stats.setPendingRevenue(pendingRev != null ? pendingRev : BigDecimal.ZERO);

        // 7. Appointments By Status
        Map<String, Integer> apptStatusMap = new HashMap<>();
        jdbcTemplate.query("SELECT status, COUNT(*) AS cnt FROM appointments GROUP BY status", rs -> {
            apptStatusMap.put(rs.getString("status"), rs.getInt("cnt"));
        });
        stats.setAppointmentsByStatus(apptStatusMap);

        // 8. Doctors By Department
        Map<String, Integer> deptMap = new HashMap<>();
        jdbcTemplate.query("SELECT department, COUNT(*) AS cnt FROM doctors GROUP BY department", rs -> {
            deptMap.put(rs.getString("department"), rs.getInt("cnt"));
        });
        stats.setDoctorsByDepartment(deptMap);

        // 9. Recent Appointments
        stats.setRecentAppointments(appointmentDAO.findAll(null, null, "ALL").stream().limit(5).toList());

        // 10. Recent Audit Logs
        stats.setRecentAuditLogs(auditLogDAO.findAll(6));

        return stats;
    }
}
