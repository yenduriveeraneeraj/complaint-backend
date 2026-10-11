package com.example.complaintmanagement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;           // JWT Access token (short lived: 15 min)
    private String refreshToken;    // JWT Refresh token (long lived: 7 days)
    private String tokenType;       // Always "Bearer"
    private Long userId;            // User's database ID
    private String fullName;        // User's display name
    private String email;           // User's email
    private String phone;           // User's phone number
    private String role;            // "ROLE_USER", "ROLE_ADMIN", or "ROLE_STAFF"
    private String message;         // "Login successful"
}
