package com.healthcare.hms.model;

import java.math.BigDecimal;

public class DiagnosticService {
    private Integer serviceId;
    private String serviceName;
    private String category;
    private BigDecimal cost;
    private Integer turnaroundHours;
    private String description;

    public DiagnosticService() {}

    public DiagnosticService(Integer serviceId, String serviceName, String category,
                             BigDecimal cost, Integer turnaroundHours, String description) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.category = category;
        this.cost = cost;
        this.turnaroundHours = turnaroundHours;
        this.description = description;
    }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public Integer getTurnaroundHours() { return turnaroundHours; }
    public void setTurnaroundHours(Integer turnaroundHours) { this.turnaroundHours = turnaroundHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
