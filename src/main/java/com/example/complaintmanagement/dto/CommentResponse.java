package com.example.complaintmanagement.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private Long authorUserId;
    private String authorName;
    private String authorRole;
    private String commentText;
    private LocalDateTime createdAt;
}
