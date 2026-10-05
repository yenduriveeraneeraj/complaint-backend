package com.example.complaintmanagement.dto;

import com.example.complaintmanagement.entity.Complaint;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * UpdateStatusRequest DTO — admin updates a complaint's status.
 * PUT /api/admin/complaints/{id}/status
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private Complaint.Status status;

    private String remarks; // Admin's note about this status change

    private Long staffId;   // Optional staff reassignment
}
