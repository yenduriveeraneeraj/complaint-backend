package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.RefreshToken;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.exception.BadRequestException;
import com.example.complaintmanagement.repository.PasswordResetTokenRepository;
import com.example.complaintmanagement.repository.TokenBlacklistRepository;
import com.example.complaintmanagement.repository.UserRepository;
import com.example.complaintmanagement.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("9876543210")
                .password("hashedPassword")
                .role(User.Role.ROLE_USER)
                .isActive(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Password@123");
        registerRequest.setConfirmPassword("Password@123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("Password@123");
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        String result = authService.register(registerRequest);

        assertEquals("Registration successful! You can now login.", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when passwords do not match during registration")
    void testRegisterPasswordMismatch() {
        registerRequest.setConfirmPassword("DifferentPassword@123");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
            authService.register(registerRequest)
        );

        assertTrue(ex.getMessage().contains("Passwords do not match"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when email is already registered")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
            authService.register(registerRequest)
        );

        assertTrue(ex.getMessage().contains("is already registered"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully authenticate and return AuthResponse")
    void testLoginSuccess() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateToken(authentication)).thenReturn("sampleJwtToken");
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        
        RefreshToken refreshToken = RefreshToken.builder().token("sampleRefreshToken").user(sampleUser).build();
        when(refreshTokenService.createRefreshToken(sampleUser.getId())).thenReturn(refreshToken);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("sampleJwtToken", response.getToken());
        assertEquals("sampleRefreshToken", response.getRefreshToken());
        assertEquals("ROLE_USER", response.getRole());
        assertEquals("john@example.com", response.getEmail());
    }
}
