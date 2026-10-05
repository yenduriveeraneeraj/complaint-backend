package com.example.complaintmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Staff Entity — maps to the 'staff' table.
 *
 * Staff members are assigned to departments
 * and then assigned to handle specific complaints.
 *
 * Note: Staff are NOT users of the system.
 * They are just records managed by the admin.
 */
@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Staff member's full name
    @NotBlank(message = "Staff name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Staff member's email
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    // Staff member's phone number
    @Column(name = "phone", length = 15)
    private String phone;

    // Job title/designation — e.g. "Network Engineer", "Electrician"
    @Column(name = "designation", length = 100)
    private String designation;

    // Is this staff member active?
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
    // Each staff member belongs to ONE department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"staffList", "complaints"})
    private Department department;

    // One staff member can be assigned to many complaints
    @OneToMany(mappedBy = "assignedStaff", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"assignedStaff", "department", "user"})
    private List<Complaint> complaints;
}
