package com.healthcare.hms.controller;

import com.healthcare.hms.model.Appointment;
import com.healthcare.hms.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return appointmentService.getAppointments(date, doctorId, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Integer id) {
        return appointmentService.getAppointmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment,
                                             @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        try {
            Appointment booked = appointmentService.bookAppointment(appointment, userRole != null ? userRole : "Frontdesk Staff");
            return ResponseEntity.status(HttpStatus.CREATED).body(booked);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Failed to book appointment: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id,
                                          @RequestBody Map<String, String> payload,
                                          @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        String newStatus = payload.get("status");
        String notes = payload.get("notes");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status field is required"));
        }

        boolean updated = appointmentService.updateStatus(id, newStatus.trim().toUpperCase(), notes, userRole != null ? userRole : "Healthcare Staff");
        if (updated) {
            return ResponseEntity.ok(Map.of("message", "Appointment status updated to " + newStatus));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Integer id,
                                               @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        boolean deleted = appointmentService.deleteAppointment(id, userRole != null ? userRole : "Frontdesk Staff");
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
