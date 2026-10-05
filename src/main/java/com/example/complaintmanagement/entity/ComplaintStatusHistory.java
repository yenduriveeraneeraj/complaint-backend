package com.example.complaintmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ComplaintStatusHistory Entity — maps to 'complaint_status_history' table.
 *
 * Every time a complaint status changes, we record it here.
 * This creates an audit trail like:
 *
 *   10:00 AM - Submitted - PENDING        (by System)
 *   11:30 AM - Assigned to IT Dept        (by Admin)
 *   01:00 PM - Work started - IN_PROGRESS (by Admin)
 *   04:30 PM - Problem solved - RESOLVED  (by Admin)
 *
 * This history is shown in the complaint details timeline.
 */
@Entity
@Table(name = "complaint_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ComplaintStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The status at this point in time
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Complaint.Status status;

    // Description of what happened — e.g., "Assigned to IT Department"
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    // Who made this change — "System", "Admin", staff name
    @Column(name = "changed_by", length = 100)
    private String changedBy;

    // When this status change happened
    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    // ── RELATIONSHIP ────────────────────────────────────────
    // Each history entry belongs to ONE complaint
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;
}
