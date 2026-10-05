package com.example.complaintmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notification Entity — maps to the 'notifications' table.
 *
 * Notifications are created automatically when:
 *   - Complaint is submitted
 *   - Complaint is assigned to department
 *   - Staff member is assigned
 *   - Status changes
 *   - Complaint is resolved
 *   - Admin adds remarks
 *
 * Example notification:
 *   "Complaint CMP-2026-00001 has been assigned to the IT Department."
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "complaint", "user"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Notification message text
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    // Has the user read this notification?
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    // When was this notification created
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── RELATIONSHIPS ──────────────────────────────────────

    // The user who receives this notification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The complaint this notification is about
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;
}
