package com.example.complaintmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter — intercepts EVERY HTTP request and checks for a JWT token.
 *
 * HOW IT WORKS:
 * ────────────────────────────────────────────────────────────
 * For every incoming request:
 *
 *   1. Check the "Authorization" header
 *   2. If it starts with "Bearer ", extract the token
 *   3. Validate the token using JwtUtils
 *   4. Load the user from the database
 *   5. Set the authentication in Spring Security context
 *   6. Continue with the request
 *
 * If no valid token → request continues WITHOUT authentication
 * → Spring Security will reject protected routes (401 Unauthorized)
 *
 * This filter runs ONCE per request (extends OncePerRequestFilter).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from request header
            String jwt = extractJwtFromRequest(request);

            // Step 2: If token exists and is valid
            if (jwt != null && jwtUtils.validateToken(jwt)) {

                // Step 3: Get email from token
                String email = jwtUtils.getEmailFromToken(jwt);

                // Step 4: Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Step 5: Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 6: Store authentication in Security Context
                // This tells Spring Security "this user is authenticated"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            System.out.println("Cannot set user authentication: " + e.getMessage());
        }

        // Continue processing the request
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * Header format: "Authorization: Bearer eyJhbGc..."
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix (7 chars)
        }
        return null;
    }
}
