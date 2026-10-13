package com.healthcare.hms.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardStats {
    private int totalPatients;
    private int totalDoctors;
    private int todayAppointments;
    private int pendingLabTests;
    private BigDecimal totalRevenue;
    private BigDecimal pendingRevenue;
    private Map<String, Integer> appointmentsByStatus = new HashMap<>();
    private Map<String, Integer> doctorsByDepartment = new HashMap<>();
    private List<Appointment> recentAppointments;
    private List<AuditLog> recentAuditLogs;

    public DashboardStats() {}

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public int getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }

    public int getTodayAppointments() { return todayAppointments; }
    public void setTodayAppointments(int todayAppointments) { this.todayAppointments = todayAppointments; }

    public int getPendingLabTests() { return pendingLabTests; }
    public void setPendingLabTests(int pendingLabTests) { this.pendingLabTests = pendingLabTests; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getPendingRevenue() { return pendingRevenue; }
    public void setPendingRevenue(BigDecimal pendingRevenue) { this.pendingRevenue = pendingRevenue; }

    public Map<String, Integer> getAppointmentsByStatus() { return appointmentsByStatus; }
    public void setAppointmentsByStatus(Map<String, Integer> appointmentsByStatus) { this.appointmentsByStatus = appointmentsByStatus; }

    public Map<String, Integer> getDoctorsByDepartment() { return doctorsByDepartment; }
    public void setDoctorsByDepartment(Map<String, Integer> doctorsByDepartment) { this.doctorsByDepartment = doctorsByDepartment; }

    public List<Appointment> getRecentAppointments() { return recentAppointments; }
    public void setRecentAppointments(List<Appointment> recentAppointments) { this.recentAppointments = recentAppointments; }

    public List<AuditLog> getRecentAuditLogs() { return recentAuditLogs; }
    public void setRecentAuditLogs(List<AuditLog> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; }
}
