package com.example.complaintmanagement.dto;

import com.example.complaintmanagement.entity.Complaint;
import lombok.*;
import java.time.LocalDateTime;

/** Response DTO for status history timeline entries */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatusHistoryResponse {
    private Long id;
    private Complaint.Status status;
    private String message;
    private String changedBy;
    private LocalDateTime changedAt;
}
