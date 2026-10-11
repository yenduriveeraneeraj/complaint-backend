package com.example.complaintmanagement.dto;

import com.example.complaintmanagement.entity.Complaint;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicComplaintTrackResponse {
    private String complaintNumber;
    private String title;
    private Complaint.Category category;
    private Complaint.Priority priority;
    private Complaint.Status status;
    private String departmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime slaDeadline;
    private Boolean isOverdue;
    private List<StatusHistoryResponse> timeline;
}
