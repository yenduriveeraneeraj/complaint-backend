package com.example.complaintmanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig — the MAIN Spring Security configuration.
 *
 * This class defines:
 *   1. Which routes are PUBLIC (no login needed)
 *   2. Which routes require ROLE_USER
 *   3. Which routes require ROLE_ADMIN
 *   4. JWT filter integration
 *   5. CORS configuration (allows React frontend to call the API)
 *   6. BCrypt password encoder
 *   7. Stateless session (no cookies — we use JWT)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Enables @PreAuthorize on individual methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * BCrypt Password Encoder.
     * Automatically hashes passwords before saving.
     * Example: "Admin@123" → "$2a$10$xyz..."
     * Comparing passwords: BCrypt does NOT reverse-decrypt,
     * it re-hashes and compares the hashes.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider — wires together:
     *   - Our UserDetailsService (loads user from DB)
     *   - BCrypt password encoder (verifies password)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication Manager — used in AuthController to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Main Security Filter Chain — defines all security rules.
     *
     * Route security rules:
     *   PUBLIC  → No JWT needed
     *   USER    → Valid JWT with ROLE_USER needed
     *   ADMIN   → Valid JWT with ROLE_ADMIN needed
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── CORS ────────────────────────────────────────────
            // Allow React frontend (localhost:5173) to call our APIs
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── CSRF ─────────────────────────────────────────────
            // Disable CSRF — we use JWT (stateless), not cookies
            .csrf(csrf -> csrf.disable())

            // ── ROUTE PERMISSIONS ────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // ✅ PUBLIC routes — anyone can access
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/departments").permitAll()

                // 🔒 ADMIN only routes
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 🔑 USER routes — any authenticated user
                .requestMatchers("/api/complaints/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/notifications/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/feedback/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/staff/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // ── SESSION ───────────────────────────────────────────
            // STATELESS: no session, no cookies — we use JWT tokens only
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── AUTH PROVIDER ─────────────────────────────────────
            .authenticationProvider(authenticationProvider())

            // ── JWT FILTER ────────────────────────────────────────
            // Add our JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration.
     *
     * CORS = Cross-Origin Resource Sharing.
     * When React (port 5173) calls Spring Boot (port 8080),
     * the browser blocks it as a "cross-origin" request.
     * We must explicitly ALLOW it here.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from React frontend URLs
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow all headers (including Authorization: Bearer ...)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow sending credentials (cookies, auth headers)
        configuration.setAllowCredentials(true);

        // Apply this CORS config to all routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
