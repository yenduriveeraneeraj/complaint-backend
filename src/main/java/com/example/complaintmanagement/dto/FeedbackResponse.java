package com.example.complaintmanagement.dto;

import lombok.*;
import java.time.LocalDateTime;

/** Feedback response DTO for admin view */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
    private Long complaintId;
    private String complaintNumber;
}
