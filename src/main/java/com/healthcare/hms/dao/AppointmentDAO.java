package com.healthcare.hms.dao;

import com.healthcare.hms.model.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AppointmentDAO {
    private final JdbcTemplate jdbcTemplate;

    public AppointmentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Appointment> rowMapper = (rs, rowNum) -> {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        Date d = rs.getDate("appointment_date");
        if (d != null) a.setAppointmentDate(d.toLocalDate());
        a.setAppointmentTime(rs.getString("appointment_time"));
        a.setReasonForVisit(rs.getString("reason_for_visit"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) a.setCreatedAt(ca.toLocalDateTime());

        // Joined fields if available
        try {
            a.setPatientName(rs.getString("patient_first_name") + " " + rs.getString("patient_last_name"));
            a.setPatientPhone(rs.getString("patient_phone"));
            a.setDoctorName(rs.getString("doctor_first_name") + " " + rs.getString("doctor_last_name"));
            a.setDoctorSpecialization(rs.getString("specialization"));
            a.setDoctorDepartment(rs.getString("department"));
            a.setConsultingFee(rs.getBigDecimal("consulting_fee"));
        } catch (Exception ignored) {}

        return a;
    };

    public List<Appointment> findAll(LocalDate date, Integer doctorId, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, " +
            "p.first_name AS patient_first_name, p.last_name AS patient_last_name, p.phone AS patient_phone, " +
            "d.first_name AS doctor_first_name, d.last_name AS doctor_last_name, d.specialization, d.department, d.consulting_fee " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN doctors d ON a.doctor_id = d.doctor_id " +
            "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (date != null) {
            sql.append("AND a.appointment_date = ? ");
            params.add(Date.valueOf(date));
        }
        if (doctorId != null) {
            sql.append("AND a.doctor_id = ? ");
            params.add(doctorId);
        }
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND UPPER(a.status) = UPPER(?) ");
            params.add(status.trim());
        }

        sql.append("ORDER BY a.appointment_date DESC, a.appointment_time ASC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<Appointment> findById(Integer appointmentId) {
        String sql = "SELECT a.*, " +
                     "p.first_name AS patient_first_name, p.last_name AS patient_last_name, p.phone AS patient_phone, " +
                     "d.first_name AS doctor_first_name, d.last_name AS doctor_last_name, d.specialization, d.department, d.consulting_fee " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                     "WHERE a.appointment_id = ?";
        List<Appointment> list = jdbcTemplate.query(sql, rowMapper, appointmentId);
        return list.stream().findFirst();
    }

    public boolean hasSlotConflict(Integer doctorId, LocalDate date, String time, Integer excludeAppointmentId) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM appointments " +
            "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status <> 'CANCELLED' "
        );
        List<Object> params = new ArrayList<>();
        params.add(doctorId);
        params.add(Date.valueOf(date));
        params.add(time);

        if (excludeAppointmentId != null) {
            sql.append("AND appointment_id <> ? ");
            params.add(excludeAppointmentId);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null && count > 0;
    }

    public Appointment save(Appointment appt) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason_for_visit, status, notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, appt.getPatientId());
            ps.setInt(2, appt.getDoctorId());
            ps.setDate(3, Date.valueOf(appt.getAppointmentDate()));
            ps.setString(4, appt.getAppointmentTime());
            ps.setString(5, appt.getReasonForVisit());
            ps.setString(6, appt.getStatus() != null ? appt.getStatus() : "SCHEDULED");
            ps.setString(7, appt.getNotes());
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    appt.setAppointmentId(n.intValue());
                    break;
                }
            }
        }
        return appt;
    }

    public boolean updateStatus(Integer appointmentId, String newStatus, String notes) {
        String sql = "UPDATE appointments SET status = ?, notes = COALESCE(?, notes) WHERE appointment_id = ?";
        return jdbcTemplate.update(sql, newStatus, notes, appointmentId) > 0;
    }

    public boolean update(Appointment appt) {
        String sql = "UPDATE appointments SET doctor_id = ?, appointment_date = ?, appointment_time = ?, reason_for_visit = ?, status = ?, notes = ? WHERE appointment_id = ?";
        return jdbcTemplate.update(sql,
                appt.getDoctorId(),
                Date.valueOf(appt.getAppointmentDate()),
                appt.getAppointmentTime(),
                appt.getReasonForVisit(),
                appt.getStatus(),
                appt.getNotes(),
                appt.getAppointmentId()) > 0;
    }

    public boolean delete(Integer appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        return jdbcTemplate.update(sql, appointmentId) > 0;
    }
}
