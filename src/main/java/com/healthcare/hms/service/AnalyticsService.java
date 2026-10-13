package com.healthcare.hms.service;

import com.healthcare.hms.dao.AnalyticsDAO;
import com.healthcare.hms.model.DashboardStats;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final AnalyticsDAO analyticsDAO;

    public AnalyticsService(AnalyticsDAO analyticsDAO) {
        this.analyticsDAO = analyticsDAO;
    }

    public DashboardStats getDashboardStats() {
        return analyticsDAO.getDashboardStats();
    }
}
