package com.healthcare.hms.service;

import com.healthcare.hms.dao.AppointmentDAO;
import com.healthcare.hms.dao.AuditLogDAO;
import com.healthcare.hms.model.Appointment;
import com.healthcare.hms.model.AuditLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentDAO appointmentDAO;
    private final AuditLogDAO auditLogDAO;

    public AppointmentService(AppointmentDAO appointmentDAO, AuditLogDAO auditLogDAO) {
        this.appointmentDAO = appointmentDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<Appointment> getAppointments(LocalDate date, Integer doctorId, String status) {
        return appointmentDAO.findAll(date, doctorId, status);
    }

    public Optional<Appointment> getAppointmentById(Integer appointmentId) {
        return appointmentDAO.findById(appointmentId);
    }

    @Transactional
    public Appointment bookAppointment(Appointment appointment, String user) {
        // Validate slot conflict
        boolean hasConflict = appointmentDAO.hasSlotConflict(
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getAppointmentId()
        );

        if (hasConflict) {
            throw new IllegalArgumentException("Schedule Conflict: Dr. is already booked for " +
                    appointment.getAppointmentDate() + " at " + appointment.getAppointmentTime() +
                    ". Please choose an alternative slot.");
        }

        Appointment saved = appointmentDAO.save(appointment);

        // Audit Trail
        AuditLog log = new AuditLog();
        log.setEntityType("APPOINTMENT");
        log.setEntityId(saved.getAppointmentId());
        log.setActionType("CREATE");
        log.setFieldName("STATUS");
        log.setOldValue(null);
        log.setNewValue("SCHEDULED");
        log.setModifiedBy(user != null ? user : "Frontdesk Staff");
        log.setReasonOrNotes("Appointment scheduled for patient ID #" + saved.getPatientId() + " on " + saved.getAppointmentDate() + " at " + saved.getAppointmentTime());
        auditLogDAO.save(log);

        return saved;
    }

    @Transactional
    public boolean updateStatus(Integer appointmentId, String newStatus, String notes, String user) {
        Optional<Appointment> existingOpt = appointmentDAO.findById(appointmentId);
        if (existingOpt.isEmpty()) return false;

        Appointment existing = existingOpt.get();
        String oldStatus = existing.getStatus();

        boolean updated = appointmentDAO.updateStatus(appointmentId, newStatus, notes);
        if (updated) {
            AuditLog log = new AuditLog();
            log.setEntityType("APPOINTMENT");
            log.setEntityId(appointmentId);
            log.setActionType("STATUS_CHANGE");
            log.setFieldName("status");
            log.setOldValue(oldStatus);
            log.setNewValue(newStatus);
            log.setModifiedBy(user != null ? user : "Healthcare Staff");
            log.setReasonOrNotes(notes != null ? notes : "Status updated from " + oldStatus + " to " + newStatus);
            auditLogDAO.save(log);
        }
        return updated;
    }

    @Transactional
    public boolean deleteAppointment(Integer appointmentId, String user) {
        AuditLog log = new AuditLog();
        log.setEntityType("APPOINTMENT");
        log.setEntityId(appointmentId);
        log.setActionType("DELETE");
        log.setFieldName("APPOINTMENT_SLOT");
        log.setOldValue("Appointment #" + appointmentId);
        log.setNewValue("CANCELLED_REMOVED");
        log.setModifiedBy(user != null ? user : "Frontdesk Staff");
        log.setReasonOrNotes("Appointment cancelled and removed from active schedule.");
        auditLogDAO.save(log);

        return appointmentDAO.delete(appointmentId);
    }
}
