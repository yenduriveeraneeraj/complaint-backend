package com.example.complaintmanagement.security;

import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CustomUserDetailsService — tells Spring Security HOW to load a user.
 *
 * Spring Security needs to load user info (including password and roles)
 * to authenticate login requests. This class does that by querying
 * our database using the UserRepository.
 *
 * The loadUserByUsername() method is called by Spring Security automatically
 * when a user tries to log in.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by email (we use email as the "username").
     * Spring Security calls this during authentication.
     *
     * @param email The user's email address
     * @return UserDetails object containing password + roles
     * @throws UsernameNotFoundException if email not found in DB
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user in database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        // Check if user account is active
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User account is deactivated: " + email);
        }

        // Return Spring's UserDetails object
        // Spring Security compares this password hash with what the user typed
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
