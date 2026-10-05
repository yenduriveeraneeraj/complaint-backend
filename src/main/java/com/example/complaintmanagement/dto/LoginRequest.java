package com.example.complaintmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * LoginRequest DTO — sent by frontend when user logs in.
 * POST /api/auth/login
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
