package com.healthcare.hms.dao;

import com.healthcare.hms.model.Doctor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DoctorDAO {
    private final JdbcTemplate jdbcTemplate;

    public DoctorDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Doctor> rowMapper = (rs, rowNum) -> {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setFirstName(rs.getString("first_name"));
        d.setLastName(rs.getString("last_name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setDepartment(rs.getString("department"));
        d.setQualification(rs.getString("qualification"));
        d.setExperienceYears(rs.getInt("experience_years"));
        d.setConsultingFee(rs.getBigDecimal("consulting_fee"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setAvailableDays(rs.getString("available_days"));
        d.setAvailableHours(rs.getString("available_hours"));
        d.setStatus(rs.getString("status"));
        return d;
    };

    public List<Doctor> findAll() {
        String sql = "SELECT * FROM doctors ORDER BY doctor_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Doctor> findById(Integer doctorId) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        List<Doctor> list = jdbcTemplate.query(sql, rowMapper, doctorId);
        return list.stream().findFirst();
    }

    public List<Doctor> findByDepartment(String department) {
        String sql = "SELECT * FROM doctors WHERE LOWER(department) = LOWER(?) ORDER BY first_name ASC";
        return jdbcTemplate.query(sql, rowMapper, department);
    }

    public Doctor save(Doctor doctor) {
        String sql = "INSERT INTO doctors (first_name, last_name, specialization, department, qualification, experience_years, consulting_fee, phone, email, available_days, available_hours, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, doctor.getFirstName());
            ps.setString(2, doctor.getLastName());
            ps.setString(3, doctor.getSpecialization());
            ps.setString(4, doctor.getDepartment());
            ps.setString(5, doctor.getQualification());
            ps.setInt(6, doctor.getExperienceYears() != null ? doctor.getExperienceYears() : 0);
            ps.setBigDecimal(7, doctor.getConsultingFee());
            ps.setString(8, doctor.getPhone());
            ps.setString(9, doctor.getEmail());
            ps.setString(10, doctor.getAvailableDays());
            ps.setString(11, doctor.getAvailableHours());
            ps.setString(12, doctor.getStatus() != null ? doctor.getStatus() : "ACTIVE");
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    doctor.setDoctorId(n.intValue());
                    break;
                }
            }
        }
        return doctor;
    }

    public boolean update(Doctor doctor) {
        String sql = "UPDATE doctors SET first_name = ?, last_name = ?, specialization = ?, department = ?, qualification = ?, experience_years = ?, consulting_fee = ?, phone = ?, email = ?, available_days = ?, available_hours = ?, status = ? WHERE doctor_id = ?";
        return jdbcTemplate.update(sql,
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization(),
                doctor.getDepartment(),
                doctor.getQualification(),
                doctor.getExperienceYears(),
                doctor.getConsultingFee(),
                doctor.getPhone(),
                doctor.getEmail(),
                doctor.getAvailableDays(),
                doctor.getAvailableHours(),
                doctor.getStatus(),
                doctor.getDoctorId()) > 0;
    }

    public boolean delete(Integer doctorId) {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        return jdbcTemplate.update(sql, doctorId) > 0;
    }
}
