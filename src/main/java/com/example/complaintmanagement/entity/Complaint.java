package com.example.complaintmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Complaint Entity — maps to the 'complaints' table.
 *
 * This is the CENTRAL entity of the system.
 * Every complaint goes through this lifecycle:
 *
 *   PENDING → IN_PROGRESS → RESOLVED
 *                         → REJECTED
 *
 * Each complaint has:
 *   - A unique complaint ID (e.g., CMP-2026-00001)
 *   - A user who submitted it
 *   - A department it's assigned to
 *   - A staff member handling it
 *   - A status history
 *   - Notifications
 *   - One feedback (after resolution)
 */
@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique complaint ID — e.g., CMP-2026-00001
    @Column(name = "complaint_number", unique = true, nullable = false, length = 20)
    private String complaintNumber;

    // Title of the complaint
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // Detailed description
    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // Category of the complaint
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private Category category;

    // Priority level
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    // Current status of the complaint
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    // Physical location of the issue
    @Column(name = "location", length = 200)
    private String location;

    // Admin's remarks/notes on the complaint
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // File attachment path (stored on server)
    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    // Original filename of the attachment
    @Column(name = "attachment_name", length = 200)
    private String attachmentName;

    // Date when complaint was resolved/closed
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // SLA deadline calculated based on priority on creation
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    // SLA Overdue flag
    @Column(name = "is_overdue", nullable = false)
    @Builder.Default
    private Boolean isOverdue = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── RELATIONSHIPS ──────────────────────────────────────

    // The user who submitted this complaint
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"complaints", "notifications", "feedbacks", "password"})
    private User user;

    // The department this complaint is assigned to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // The staff member assigned to handle this complaint
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff assignedStaff;

    // Status change history — every status change is recorded
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("changedAt DESC")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ComplaintStatusHistory> statusHistory;

    // Notifications sent about this complaint
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Notification> notifications;

    // One feedback per complaint (after resolution)
    @OneToOne(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Feedback feedback;

    // ── ENUMS ──────────────────────────────────────────────

    public enum Category {
        ELECTRICAL,
        WATER,
        INTERNET_WIFI,
        CLEANLINESS,
        MAINTENANCE,
        TRANSPORT,
        SECURITY,
        OTHER
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum Status {
        PENDING,
        IN_PROGRESS,
        RESOLVED,
        REJECTED
    }
}
