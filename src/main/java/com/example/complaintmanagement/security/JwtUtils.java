package com.example.complaintmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtUtils — generates and validates JWT tokens.
 *
 * HOW JWT WORKS:
 * ─────────────────────────────────────────────────
 * 1. User logs in → we create a token containing their email
 * 2. Token is sent to the React frontend
 * 3. Frontend stores token in localStorage
 * 4. Every API request includes: "Authorization: Bearer <token>"
 * 5. Spring Boot validates the token → identifies the user
 * 6. No session/cookie needed! (stateless)
 *
 * JWT Token Structure:
 *   header.payload.signature
 *   eyJhbGc...  .eyJzdWIiOiJ...  .SflKxwRJSMeKKF2...
 *
 * Payload contains:
 *   - sub: user email
 *   - iat: issued at timestamp
 *   - exp: expiration timestamp
 */
@Component
public class JwtUtils {

    // Injected from application.properties
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generate JWT token after successful login.
     * The token contains the user's email as the subject.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateTokenFromEmail(userDetails.getUsername());
    }

    public String generateTokenFromEmail(String email) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(email)                              // who is this token for?
                .setIssuedAt(new Date())                        // when was it created?
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // when does it expire?
                .signWith(key, SignatureAlgorithm.HS256)        // sign with secret key
                .compact();
    }

    /**
     * Extract email from a JWT token.
     * Called when validating incoming requests.
     */
    public String getEmailFromToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validate a JWT token.
     * Returns true if valid, false if expired/tampered/invalid.
     */
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            System.out.println("JWT Error: Invalid token format");
        } catch (ExpiredJwtException e) {
            System.out.println("JWT Error: Token has expired");
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT Error: Token not supported");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT Error: Token claims string is empty");
        }
        return false;
    }
}
