package com.healthcare.hms.model;

import java.math.BigDecimal;

public class Doctor {
    private Integer doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String department;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultingFee;
    private String phone;
    private String email;
    private String availableDays;
    private String availableHours;
    private String status;

    public Doctor() {}

    public Doctor(Integer doctorId, String firstName, String lastName, String specialization,
                  String department, String qualification, Integer experienceYears,
                  BigDecimal consultingFee, String phone, String email,
                  String availableDays, String availableHours, String status) {
        this.doctorId = doctorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.department = department;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultingFee = consultingFee;
        this.phone = phone;
        this.email = email;
        this.availableDays = availableDays;
        this.availableHours = availableHours;
        this.status = status;
    }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public BigDecimal getConsultingFee() { return consultingFee; }
    public void setConsultingFee(BigDecimal consultingFee) { this.consultingFee = consultingFee; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public String getAvailableHours() { return availableHours; }
    public void setAvailableHours(String availableHours) { this.availableHours = availableHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
