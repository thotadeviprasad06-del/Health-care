package com.healthcare.hms.controller;

import com.healthcare.hms.model.MedicalRecord;
import com.healthcare.hms.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping
    public List<MedicalRecord> getRecords(@RequestParam(required = false) Integer patientId) {
        if (patientId != null) {
            return medicalRecordService.getRecordsByPatientId(patientId);
        }
        return medicalRecordService.getAllRecords();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> getRecordById(@PathVariable Integer id) {
        return medicalRecordService.getRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MedicalRecord> createRecord(@RequestBody MedicalRecord record,
                                                      @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        MedicalRecord created = medicalRecordService.createRecord(record, userRole != null ? userRole : "Attending Physician");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
