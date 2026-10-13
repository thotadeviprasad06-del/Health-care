package com.healthcare.hms.controller;

import com.healthcare.hms.model.Patient;
import com.healthcare.hms.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<Patient> getAllPatients(@RequestParam(required = false) String search) {
        return patientService.getAllPatients(search);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Integer id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Patient> registerPatient(@RequestBody Patient patient,
                                                  @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        Patient created = patientService.registerPatient(patient, userRole != null ? userRole : "Healthcare Staff");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Integer id,
                                          @RequestBody Patient patient,
                                          @RequestParam(required = false) String reason,
                                          @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        patient.setPatientId(id);
        boolean updated = patientService.updatePatient(patient, userRole != null ? userRole : "Healthcare Staff", reason);
        if (updated) {
            return ResponseEntity.ok(Map.of("message", "Patient updated successfully", "patientId", id));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable Integer id,
                                          @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        boolean deleted = patientService.deletePatient(id, userRole != null ? userRole : "Healthcare Administrator");
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Patient record deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
