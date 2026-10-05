package com.example.complaintmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Feedback Entity — maps to the 'feedbacks' table.
 *
 * After a complaint is RESOLVED, the user can:
 *   - Rate the resolution (1 to 5 stars)
 *   - Leave a comment
 *
 * Business Rules:
 *   - Only allowed AFTER complaint is RESOLVED
 *   - Only ONE feedback per complaint
 *   - Cannot be edited once submitted
 */
@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "complaint", "user"})
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Rating — 1 (worst) to 5 (best) stars
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    @Column(name = "rating", nullable = false)
    private Integer rating;

    // Optional written feedback
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── RELATIONSHIPS ──────────────────────────────────────

    // The user who gave this feedback
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The complaint this feedback is for (one-to-one)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false, unique = true)
    private Complaint complaint;
}
