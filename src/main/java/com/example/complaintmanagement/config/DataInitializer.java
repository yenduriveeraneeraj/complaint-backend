package com.example.complaintmanagement.config;

import com.example.complaintmanagement.entity.*;
import com.example.complaintmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final StaffRepository staffRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking data initialization...");

        // 1. Seed Admin User
        User admin = userRepository.findByEmail("admin@cms.com").orElseGet(() -> {
            User newAdmin = User.builder()
                    .fullName("System Administrator")
                    .email("admin@cms.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .phone("9876543210")
                    .role(User.Role.ROLE_ADMIN)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            log.info("Seeding default Admin user (admin@cms.com)");
            return userRepository.save(newAdmin);
        });

        // 2. Seed Regular User
        User regularUser = userRepository.findByEmail("user@cms.com").orElseGet(() -> {
            User newUser = User.builder()
                    .fullName("John Doe")
                    .email("user@cms.com")
                    .password(passwordEncoder.encode("User@123"))
                    .phone("9123456789")
                    .role(User.Role.ROLE_USER)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            log.info("Seeding default regular user (user@cms.com)");
            return userRepository.save(newUser);
        });

        // 3. Seed Departments if empty
        if (departmentRepository.count() == 0) {
            log.info("Seeding departments...");
            Department dept1 = departmentRepository.save(Department.builder()
                    .name("IT & Technical Support")
                    .description("Handles computer hardware, software, server and connectivity issues")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            Department dept2 = departmentRepository.save(Department.builder()
                    .name("Water & Sanitation")
                    .description("Handles water supply, pipe leaks, sewage and drainage problems")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            Department dept3 = departmentRepository.save(Department.builder()
                    .name("Electricity & Power")
                    .description("Handles power outages, faulty lines, street lights and transformers")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            Department dept4 = departmentRepository.save(Department.builder()
                    .name("Roads & Infrastructure")
                    .description("Handles potholes, road damage, traffic lights and footpaths")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            // Seed Staff
            Staff staff1 = staffRepository.save(Staff.builder()
                    .name("Alex Rivera")
                    .email("alex.it@cms.com")
                    .phone("9888111222")
                    .department(dept1)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            Staff staff2 = staffRepository.save(Staff.builder()
                    .name("Sarah Connor")
                    .email("sarah.water@cms.com")
                    .phone("9888333444")
                    .department(dept2)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            Staff staff3 = staffRepository.save(Staff.builder()
                    .name("Michael Vance")
                    .email("michael.power@cms.com")
                    .phone("9888555666")
                    .department(dept3)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            // Seed sample complaints
            if (complaintRepository.count() == 0) {
                complaintRepository.save(Complaint.builder()
                        .complaintNumber("CMP-2026-00001")
                        .title("Internet Outage in Sector 4")
                        .description("High-speed optical fiber connection down since morning across Block C.")
                        .category(Complaint.Category.INTERNET_WIFI)
                        .priority(Complaint.Priority.HIGH)
                        .status(Complaint.Status.IN_PROGRESS)
                        .department(dept1)
                        .assignedStaff(staff1)
                        .user(regularUser)
                        .location("Sector 4, Block C")
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .updatedAt(LocalDateTime.now().minusDays(1))
                        .build());

                complaintRepository.save(Complaint.builder()
                        .complaintNumber("CMP-2026-00002")
                        .title("Water Pipeline Leakage near Main Market")
                        .description("Clean drinking water leaking heavily from underground pipe.")
                        .category(Complaint.Category.WATER)
                        .priority(Complaint.Priority.URGENT)
                        .status(Complaint.Status.PENDING)
                        .department(dept2)
                        .user(regularUser)
                        .location("Main Market Gate 2")
                        .createdAt(LocalDateTime.now().minusHours(5))
                        .updatedAt(LocalDateTime.now().minusHours(5))
                        .build());

                complaintRepository.save(Complaint.builder()
                        .complaintNumber("CMP-2026-00003")
                        .title("Street Light Not Working on 5th Avenue")
                        .description("Three consecutive poles dark, causing safety concerns at night.")
                        .category(Complaint.Category.ELECTRICAL)
                        .priority(Complaint.Priority.MEDIUM)
                        .status(Complaint.Status.RESOLVED)
                        .department(dept3)
                        .assignedStaff(staff3)
                        .user(regularUser)
                        .location("5th Avenue, Crossroad 12")
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .updatedAt(LocalDateTime.now().minusDays(1))
                        .build());
            }
        }

        log.info("Data initialization complete!");
    }
}
