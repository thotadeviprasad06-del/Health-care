package com.healthcare.hms.dao;

import com.healthcare.hms.model.MedicalRecord;
import com.healthcare.hms.model.Prescription;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MedicalRecordDAO {
    private final JdbcTemplate jdbcTemplate;

    public MedicalRecordDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MedicalRecord> recordRowMapper = (rs, rowNum) -> {
        MedicalRecord r = new MedicalRecord();
        r.setRecordId(rs.getInt("record_id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setDoctorId(rs.getInt("doctor_id"));
        r.setAppointmentId(rs.getObject("appointment_id") != null ? rs.getInt("appointment_id") : null);
        Date d = rs.getDate("record_date");
        if (d != null) r.setRecordDate(d.toLocalDate());
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setSymptoms(rs.getString("symptoms"));
        r.setBloodPressure(rs.getString("blood_pressure"));
        r.setHeartRate(rs.getObject("heart_rate") != null ? rs.getInt("heart_rate") : null);
        r.setTemperature(rs.getBigDecimal("temperature"));
        r.setWeightKg(rs.getBigDecimal("weight_kg"));
        r.setTreatmentPlan(rs.getString("treatment_plan"));
        r.setDoctorNotes(rs.getString("doctor_notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) r.setCreatedAt(ca.toLocalDateTime());

        try {
            r.setPatientName(rs.getString("patient_first_name") + " " + rs.getString("patient_last_name"));
            r.setDoctorName(rs.getString("doctor_first_name") + " " + rs.getString("doctor_last_name"));
            r.setDoctorSpecialization(rs.getString("specialization"));
        } catch (Exception ignored) {}

        return r;
    };

    private final RowMapper<Prescription> prescriptionRowMapper = (rs, rowNum) -> {
        Prescription p = new Prescription();
        p.setPrescriptionId(rs.getInt("prescription_id"));
        p.setRecordId(rs.getInt("record_id"));
        p.setMedicineName(rs.getString("medicine_name"));
        p.setDosage(rs.getString("dosage"));
        p.setFrequency(rs.getString("frequency"));
        p.setDuration(rs.getString("duration"));
        p.setInstructions(rs.getString("instructions"));
        return p;
    };

    public List<MedicalRecord> findAll() {
        String sql = "SELECT r.*, " +
                     "p.first_name AS patient_first_name, p.last_name AS patient_last_name, " +
                     "d.first_name AS doctor_first_name, d.last_name AS doctor_last_name, d.specialization " +
                     "FROM medical_records r " +
                     "JOIN patients p ON r.patient_id = p.patient_id " +
                     "JOIN doctors d ON r.doctor_id = d.doctor_id " +
                     "ORDER BY r.record_date DESC, r.record_id DESC";
        List<MedicalRecord> list = jdbcTemplate.query(sql, recordRowMapper);
        for (MedicalRecord record : list) {
            record.setPrescriptions(findPrescriptionsByRecordId(record.getRecordId()));
        }
        return list;
    }

    public List<MedicalRecord> findByPatientId(Integer patientId) {
        String sql = "SELECT r.*, " +
                     "p.first_name AS patient_first_name, p.last_name AS patient_last_name, " +
                     "d.first_name AS doctor_first_name, d.last_name AS doctor_last_name, d.specialization " +
                     "FROM medical_records r " +
                     "JOIN patients p ON r.patient_id = p.patient_id " +
                     "JOIN doctors d ON r.doctor_id = d.doctor_id " +
                     "WHERE r.patient_id = ? " +
                     "ORDER BY r.record_date DESC, r.record_id DESC";
        List<MedicalRecord> list = jdbcTemplate.query(sql, recordRowMapper, patientId);
        for (MedicalRecord record : list) {
            record.setPrescriptions(findPrescriptionsByRecordId(record.getRecordId()));
        }
        return list;
    }

    public Optional<MedicalRecord> findById(Integer recordId) {
        String sql = "SELECT r.*, " +
                     "p.first_name AS patient_first_name, p.last_name AS patient_last_name, " +
                     "d.first_name AS doctor_first_name, d.last_name AS doctor_last_name, d.specialization " +
                     "FROM medical_records r " +
                     "JOIN patients p ON r.patient_id = p.patient_id " +
                     "JOIN doctors d ON r.doctor_id = d.doctor_id " +
                     "WHERE r.record_id = ?";
        List<MedicalRecord> list = jdbcTemplate.query(sql, recordRowMapper, recordId);
        if (list.isEmpty()) return Optional.empty();

        MedicalRecord record = list.get(0);
        record.setPrescriptions(findPrescriptionsByRecordId(recordId));
        return Optional.of(record);
    }

    public MedicalRecord save(MedicalRecord record) {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, appointment_id, record_date, diagnosis, symptoms, blood_pressure, heart_rate, temperature, weight_kg, treatment_plan, doctor_notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, record.getPatientId());
            ps.setInt(2, record.getDoctorId());
            if (record.getAppointmentId() != null) {
                ps.setInt(3, record.getAppointmentId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setDate(4, Date.valueOf(record.getRecordDate()));
            ps.setString(5, record.getDiagnosis());
            ps.setString(6, record.getSymptoms());
            ps.setString(7, record.getBloodPressure());
            if (record.getHeartRate() != null) {
                ps.setInt(8, record.getHeartRate());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            ps.setBigDecimal(9, record.getTemperature());
            ps.setBigDecimal(10, record.getWeightKg());
            ps.setString(11, record.getTreatmentPlan());
            ps.setString(12, record.getDoctorNotes());
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    record.setRecordId(n.intValue());
                    break;
                }
            }
        }

        // Save Prescriptions if any
        if (record.getPrescriptions() != null) {
            for (Prescription p : record.getPrescriptions()) {
                p.setRecordId(record.getRecordId());
                savePrescription(p);
            }
        }

        return record;
    }

    public List<Prescription> findPrescriptionsByRecordId(Integer recordId) {
        String sql = "SELECT * FROM prescriptions WHERE record_id = ? ORDER BY prescription_id ASC";
        return jdbcTemplate.query(sql, prescriptionRowMapper, recordId);
    }

    public Prescription savePrescription(Prescription p) {
        String sql = "INSERT INTO prescriptions (record_id, medicine_name, dosage, frequency, duration, instructions) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getRecordId());
            ps.setString(2, p.getMedicineName());
            ps.setString(3, p.getDosage());
            ps.setString(4, p.getFrequency());
            ps.setString(5, p.getDuration());
            ps.setString(6, p.getInstructions());
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    p.setPrescriptionId(n.intValue());
                    break;
                }
            }
        }
        return p;
    }
}
