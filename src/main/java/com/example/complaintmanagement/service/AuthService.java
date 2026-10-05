package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.exception.BadRequestException;
import com.example.complaintmanagement.repository.UserRepository;
import com.example.complaintmanagement.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService — handles user registration and login.
 *
 * REGISTRATION FLOW:
 *   1. Validate that passwords match
 *   2. Check if email already exists
 *   3. Hash the password using BCrypt
 *   4. Save user to database
 *   5. Return success message
 *
 * LOGIN FLOW:
 *   1. Spring Security authenticates email + password
 *   2. Generate JWT token
 *   3. Return token + user details to frontend
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Register a new user.
     */
    public String register(RegisterRequest request) {

        // Check passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match. Please try again.");
        }

        // Check email is not already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                "Email '" + request.getEmail() + "' is already registered. Please login instead."
            );
        }

        // Build user entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                // BCrypt hash the password — NEVER store plain text passwords!
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ROLE_USER)
                .isActive(true)
                .build();

        userRepository.save(user);

        return "Registration successful! You can now login.";
    }

    /**
     * Authenticate user and return JWT token.
     */
    public AuthResponse login(LoginRequest request) {

        // Spring Security authenticates the email + password
        // This internally calls CustomUserDetailsService.loadUserByUsername()
        // and compares BCrypt hashed passwords
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Generate JWT token for this authenticated user
        String token = jwtUtils.generateToken(authentication);

        // Load user details to include in the response
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .message("Login successful! Welcome, " + user.getFullName())
                .build();
    }
}
