package com.example.complaintmanagement.dto;

import com.example.complaintmanagement.entity.Complaint;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ComplaintResponse DTO — sent to frontend when listing/viewing complaints.
 *
 * This is what the user/admin sees in the complaint list and detail views.
 * We include department name and staff name as strings (not full objects)
 * to keep the response clean and avoid circular references.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {

    private Long id;
    private String complaintNumber;
    private String title;
    private String description;
    private Complaint.Category category;
    private Complaint.Priority priority;
    private Complaint.Status status;
    private String location;
    private String remarks;
    private String attachmentName;
    private String attachmentPath;

    // User info
    private Long userId;
    private String userName;
    private String userEmail;

    // Department info (null if not yet assigned)
    private Long departmentId;
    private String departmentName;

    // Staff info (null if not yet assigned)
    private Long staffId;
    private String staffName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
