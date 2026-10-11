package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.PasswordResetToken;
import com.example.complaintmanagement.entity.RefreshToken;
import com.example.complaintmanagement.entity.TokenBlacklist;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.exception.BadRequestException;
import com.example.complaintmanagement.exception.ResourceNotFoundException;
import com.example.complaintmanagement.repository.PasswordResetTokenRepository;
import com.example.complaintmanagement.repository.TokenBlacklistRepository;
import com.example.complaintmanagement.repository.UserRepository;
import com.example.complaintmanagement.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final EmailService emailService;

    /**
     * Register a new user.
     */
    public String register(RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match. Please try again.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                "Email '" + request.getEmail() + "' is already registered. Please login instead."
            );
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ROLE_USER)
                .isActive(true)
                .build();

        userRepository.save(user);

        return "Registration successful! You can now login.";
    }

    /**
     * Authenticate user and return JWT Access + Refresh token.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken = jwtUtils.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .message("Login successful! Welcome, " + user.getFullName())
                .build();
    }

    /**
     * Refresh JWT token using a valid RefreshToken.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtUtils.generateTokenFromEmail(user.getEmail());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
                    return AuthResponse.builder()
                            .token(accessToken)
                            .refreshToken(newRefreshToken.getToken())
                            .tokenType("Bearer")
                            .userId(user.getId())
                            .fullName(user.getFullName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .role(user.getRole().name())
                            .message("Token refreshed successfully!")
                            .build();
                })
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database!"));
    }

    /**
     * Logout and invalidate tokens.
     */
    @Transactional
    public String logout(String authHeader, String refreshTokenStr) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!tokenBlacklistRepository.existsByToken(token)) {
                tokenBlacklistRepository.save(TokenBlacklist.builder()
                        .token(token)
                        .blacklistedAt(LocalDateTime.now())
                        .build());
            }
        }
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenService.findByToken(refreshTokenStr)
                    .ifPresent(token -> refreshTokenService.deleteByUserId(token.getUser().getId()));
        }
        return "Logged out successfully.";
    }

    /**
     * Send 6-digit OTP for Forgot Password flow.
     */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User email not found: " + request.getEmail()));

        // Delete existing reset tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate 6-digit OTP
        String otp = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .otp(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        passwordResetTokenRepository.save(token);

        log.info("Generated Password Reset OTP for {}: {}", user.getEmail(), otp);

        // Send OTP email
        try {
            emailService.sendPasswordResetEmail(user, otp);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", user.getEmail(), e);
        }

        return "Password reset OTP has been sent to your email (" + user.getEmail() + ").";
    }

    /**
     * Reset password with OTP.
     */
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User email not found: " + request.getEmail()));

        PasswordResetToken resetToken = passwordResetTokenRepository.findByOtpAndUserAndUsedFalse(request.getOtp(), user)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return "Password has been reset successfully. You can now login with your new password.";
    }
}
