package com.healthcare.hms.service;

import com.healthcare.hms.dao.DoctorDAO;
import com.healthcare.hms.model.Doctor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorDAO doctorDAO;

    public DoctorService(DoctorDAO doctorDAO) {
        this.doctorDAO = doctorDAO;
    }

    public List<Doctor> getAllDoctors() {
        return doctorDAO.findAll();
    }

    public Optional<Doctor> getDoctorById(Integer doctorId) {
        return doctorDAO.findById(doctorId);
    }

    public List<Doctor> getDoctorsByDepartment(String department) {
        return doctorDAO.findByDepartment(department);
    }

    public Doctor saveDoctor(Doctor doctor) {
        return doctorDAO.save(doctor);
    }

    public boolean updateDoctor(Doctor doctor) {
        return doctorDAO.update(doctor);
    }

    public boolean deleteDoctor(Integer doctorId) {
        return doctorDAO.delete(doctorId);
    }
}
