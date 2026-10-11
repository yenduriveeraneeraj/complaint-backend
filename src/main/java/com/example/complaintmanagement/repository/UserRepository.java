package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * UserRepository — provides all database operations for User.
 *
 * JpaRepository<User, Long> gives us these methods for FREE:
 *   save()      → INSERT or UPDATE
 *   findById()  → SELECT WHERE id = ?
 *   findAll()   → SELECT all rows
 *   delete()    → DELETE
 *   count()     → COUNT(*)
 *
 * We add CUSTOM queries below using method naming convention:
 *   findByEmail → SELECT * FROM users WHERE email = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email — used during login
    Optional<User> findByEmail(String email);

    // Check if email already exists — used during registration
    boolean existsByEmail(String email);

    // Find all active users
    List<User> findByIsActive(Boolean isActive);

    // Search users by name or email (for admin search)
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(String keyword);

    // Count total users
    long countByRole(User.Role role);

    // Find users by role
    List<User> findByRole(User.Role role);

    // Count active users
    long countByIsActive(Boolean isActive);
}
