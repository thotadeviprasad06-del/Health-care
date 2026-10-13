package com.healthcare.hms.controller;

import com.healthcare.hms.model.DiagnosticService;
import com.healthcare.hms.model.DiagnosticTest;
import com.healthcare.hms.service.LabDiagnosticService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostics")
@CrossOrigin(origins = "*")
public class DiagnosticController {
    private final LabDiagnosticService labDiagnosticService;

    public DiagnosticController(LabDiagnosticService labDiagnosticService) {
        this.labDiagnosticService = labDiagnosticService;
    }

    @GetMapping("/services")
    public List<DiagnosticService> getCatalogServices() {
        return labDiagnosticService.getAllCatalogServices();
    }

    @GetMapping("/tests")
    public List<DiagnosticTest> getBookedTests(@RequestParam(required = false) Integer patientId,
                                               @RequestParam(required = false, defaultValue = "ALL") String status) {
        return labDiagnosticService.getAllBookedTests(patientId, status);
    }

    @GetMapping("/tests/{id}")
    public ResponseEntity<DiagnosticTest> getTestById(@PathVariable Integer id) {
        return labDiagnosticService.getTestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tests")
    public ResponseEntity<DiagnosticTest> orderTest(@RequestBody DiagnosticTest test,
                                                   @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        DiagnosticTest created = labDiagnosticService.orderTest(test, userRole != null ? userRole : "Lab Technician");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/tests/{id}/results")
    public ResponseEntity<?> updateResults(@PathVariable Integer id,
                                          @RequestBody Map<String, String> payload,
                                          @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        String status = payload.getOrDefault("status", "COMPLETED");
        String results = payload.get("results");
        String normalRange = payload.get("normalRange");
        String remarks = payload.get("remarks");

        boolean updated = labDiagnosticService.updateTestResults(id, status, results, normalRange, remarks, userRole != null ? userRole : "Pathologist");
        if (updated) {
            return ResponseEntity.ok(Map.of("message", "Diagnostic test results and status updated successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
