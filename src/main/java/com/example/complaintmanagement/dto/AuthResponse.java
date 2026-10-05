package com.example.complaintmanagement.dto;

import lombok.*;

/**
 * AuthResponse DTO — sent back to frontend after successful login.
 *
 * The JWT token is stored in the browser (localStorage).
 * Every subsequent API request includes this token in the
 * Authorization header: "Bearer <token>"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;           // JWT token — stored in frontend
    private String tokenType;       // Always "Bearer"
    private Long userId;            // User's database ID
    private String fullName;        // User's display name
    private String email;           // User's email
    private String phone;           // User's phone number
    private String role;            // "ROLE_USER" or "ROLE_ADMIN"
    private String message;         // "Login successful"
}
