package com.example.complaintmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ====================================================
 * ComplaintManagementApplication.java
 * ====================================================
 * This is the ENTRY POINT of our Spring Boot application.
 *
 * @SpringBootApplication is a combination of 3 annotations:
 *   1. @Configuration       — marks this as a config class
 *   2. @EnableAutoConfiguration — auto-configures Spring Boot
 *   3. @ComponentScan       — scans for components in this package
 *
 * When you run this class, Spring Boot starts an embedded
 * Tomcat server on port 8080 by default.
 * ====================================================
 */
@SpringBootApplication
public class ComplaintManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplaintManagementApplication.class, args);
        System.out.println("====================================================");
        System.out.println("  Online Complaint Management System — STARTED");
        System.out.println("  Backend running at: http://localhost:8080");
        System.out.println("====================================================");
    }
}
