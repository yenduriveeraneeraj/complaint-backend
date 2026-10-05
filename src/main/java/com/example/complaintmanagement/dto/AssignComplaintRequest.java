package com.example.complaintmanagement.dto;

import com.example.complaintmanagement.entity.Complaint;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * AssignComplaintRequest DTO — admin assigns a complaint to dept + staff.
 * PUT /api/admin/complaints/{id}/assign
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssignComplaintRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long staffId; // Optional — can assign dept first, staff later

    private String remarks;
}
