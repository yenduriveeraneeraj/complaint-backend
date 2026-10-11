package com.example.complaintmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Entity — maps to the 'users' table in MySQL.
 *
 * Annotations explained:
 *   @Entity      → tells JPA this class is a database table
 *   @Table       → specifies the exact table name
 *   @Id          → marks the primary key
 *   @GeneratedValue → auto-increment the ID
 *   @Column      → maps field to a column
 *   @OneToMany   → one user has many complaints
 *   @Enumerated  → store enum as string (not number)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Full Name — required, max 100 characters
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    // Email — must be unique and valid format
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    // Phone Number — Indian phone format validation
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Please provide a valid 10-digit phone number")
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    // Password — stored as BCrypt hash (never plain text!)
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    // Role — either ROLE_USER or ROLE_ADMIN
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    // Is the account active? Admin can deactivate users
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Automatically set when user is created
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Automatically updated on every save
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── RELATIONSHIPS ──────────────────────────────────────
    // One user can submit many complaints
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Complaint> complaints;

    // One user can have many notifications
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Notification> notifications;

    // One user can give one feedback per complaint
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Feedback> feedbacks;

    // ── ENUM: User Roles ───────────────────────────────────
    public enum Role {
        ROLE_USER,   // Regular user — can submit complaints
        ROLE_ADMIN,  // Admin — can manage everything
        ROLE_STAFF   // Staff/Technician — handles assigned complaints
    }
}
