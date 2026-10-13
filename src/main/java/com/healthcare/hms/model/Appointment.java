package com.healthcare.hms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Appointment {
    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String reasonForVisit;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    // Joined fields for display
    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String doctorSpecialization;
    private String doctorDepartment;
    private BigDecimal consultingFee;

    public Appointment() {}

    public Appointment(Integer appointmentId, Integer patientId, Integer doctorId,
                       LocalDate appointmentDate, String appointmentTime,
                       String reasonForVisit, String status, String notes) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reasonForVisit = reasonForVisit;
        this.status = status;
        this.notes = notes;
    }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getReasonForVisit() { return reasonForVisit; }
    public void setReasonForVisit(String reasonForVisit) { this.reasonForVisit = reasonForVisit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public String getDoctorDepartment() { return doctorDepartment; }
    public void setDoctorDepartment(String doctorDepartment) { this.doctorDepartment = doctorDepartment; }

    public BigDecimal getConsultingFee() { return consultingFee; }
    public void setConsultingFee(BigDecimal consultingFee) { this.consultingFee = consultingFee; }
}
