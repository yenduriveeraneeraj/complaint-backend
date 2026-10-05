package com.example.complaintmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/** Request DTO for submitting feedback */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackRequest {

    @NotNull(message = "Complaint ID is required")
    private Long complaintId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;
}
