package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — handles /api/auth/register and /api/auth/login
 *
 * These are PUBLIC routes — no JWT token needed.
 * React's register and login forms POST to these endpoints.
 *
 * @RestController  = @Controller + @ResponseBody (auto-converts to JSON)
 * @RequestMapping  = base URL prefix for all methods in this class
 * @CrossOrigin     = allows React frontend to call these endpoints
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     *
     * Request body (JSON from React):
     * {
     *   "fullName": "John Doe",
     *   "email": "john@example.com",
     *   "phone": "9876543210",
     *   "password": "John@123",
     *   "confirmPassword": "John@123"
     * }
     *
     * @Valid triggers validation annotations on RegisterRequest
     * Returns 201 Created on success
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * POST /api/auth/login
     *
     * Request body:
     * { "email": "john@example.com", "password": "John@123" }
     *
     * Response:
     * { "token": "eyJhbGc...", "role": "ROLE_USER", "fullName": "John Doe", ... }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
