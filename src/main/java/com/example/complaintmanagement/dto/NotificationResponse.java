package com.example.complaintmanagement.dto;

import lombok.*;
import java.time.LocalDateTime;

/** Notification DTO sent to frontend */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private Long complaintId;
    private String complaintNumber;
}
