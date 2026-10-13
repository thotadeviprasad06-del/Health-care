package com.healthcare.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HealthManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthManagementSystemApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" HEALTH MANAGEMENT SYSTEM (HMS) READY AT: http://localhost:8085   ");
        System.out.println("==================================================================");
    }
}
