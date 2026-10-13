package com.healthcare.hms.controller;

import com.healthcare.hms.model.Doctor;
import com.healthcare.hms.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<Doctor> getAllDoctors(@RequestParam(required = false) String department) {
        if (department != null && !department.trim().isEmpty()) {
            return doctorService.getDoctorsByDepartment(department);
        }
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Integer id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Doctor> addDoctor(@RequestBody Doctor doctor) {
        Doctor saved = doctorService.saveDoctor(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Integer id, @RequestBody Doctor doctor) {
        doctor.setDoctorId(id);
        boolean updated = doctorService.updateDoctor(doctor);
        if (updated) {
            return ResponseEntity.ok(Map.of("message", "Doctor profile updated successfully"));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Integer id) {
        boolean deleted = doctorService.deleteDoctor(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Doctor record deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
