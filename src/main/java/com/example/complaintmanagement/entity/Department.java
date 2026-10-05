package com.example.complaintmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Department Entity — maps to the 'departments' table.
 *
 * Examples:
 *   IT Department, Maintenance Department,
 *   Electrical Department, Security Department, etc.
 *
 * Each department can have many staff members
 * and handle many complaints.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Department name — e.g. "IT Department"
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    // Short description of the department
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Is this department active?
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── RELATIONSHIPS ──────────────────────────────────────
    // One department has many staff members
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"department", "complaints"})
    private List<Staff> staffList;

    // One department handles many complaints
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"department", "assignedStaff", "user"})
    private List<Complaint> complaints;
}
