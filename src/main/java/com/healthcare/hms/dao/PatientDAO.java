package com.healthcare.hms.dao;

import com.healthcare.hms.model.Patient;
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
public class PatientDAO {
    private final JdbcTemplate jdbcTemplate;

    public PatientDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Patient> rowMapper = (rs, rowNum) -> {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        Date dob = rs.getDate("dob");
        if (dob != null) p.setDob(dob.toLocalDate());
        p.setGender(rs.getString("gender"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContactName(rs.getString("emergency_contact_name"));
        p.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        p.setAllergies(rs.getString("allergies"));
        p.setMedicalHistory(rs.getString("medical_history"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) p.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) p.setUpdatedAt(ua.toLocalDateTime());
        return p;
    };

    public List<Patient> findAll(String search) {
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            String sql = "SELECT * FROM patients WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR phone LIKE ? OR LOWER(email) LIKE ? ORDER BY patient_id DESC";
            return jdbcTemplate.query(sql, rowMapper, pattern, pattern, pattern, pattern);
        }
        String sql = "SELECT * FROM patients ORDER BY patient_id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Patient> findById(Integer patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        List<Patient> list = jdbcTemplate.query(sql, rowMapper, patientId);
        return list.stream().findFirst();
    }

    public Patient save(Patient patient) {
        String sql = "INSERT INTO patients (first_name, last_name, dob, gender, blood_group, phone, email, address, emergency_contact_name, emergency_contact_phone, allergies, medical_history, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, patient.getFirstName());
            ps.setString(2, patient.getLastName());
            ps.setDate(3, patient.getDob() != null ? Date.valueOf(patient.getDob()) : null);
            ps.setString(4, patient.getGender());
            ps.setString(5, patient.getBloodGroup());
            ps.setString(6, patient.getPhone());
            ps.setString(7, patient.getEmail());
            ps.setString(8, patient.getAddress());
            ps.setString(9, patient.getEmergencyContactName());
            ps.setString(10, patient.getEmergencyContactPhone());
            ps.setString(11, patient.getAllergies());
            ps.setString(12, patient.getMedicalHistory());
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    patient.setPatientId(n.intValue());
                    break;
                }
            }
        }
        return patient;
    }

    public boolean update(Patient patient) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, dob = ?, gender = ?, blood_group = ?, phone = ?, email = ?, address = ?, emergency_contact_name = ?, emergency_contact_phone = ?, allergies = ?, medical_history = ?, updated_at = CURRENT_TIMESTAMP WHERE patient_id = ?";
        int rows = jdbcTemplate.update(sql,
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDob() != null ? Date.valueOf(patient.getDob()) : null,
                patient.getGender(),
                patient.getBloodGroup(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getEmergencyContactName(),
                patient.getEmergencyContactPhone(),
                patient.getAllergies(),
                patient.getMedicalHistory(),
                patient.getPatientId());
        return rows > 0;
    }

    public boolean delete(Integer patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        return jdbcTemplate.update(sql, patientId) > 0;
    }
}
