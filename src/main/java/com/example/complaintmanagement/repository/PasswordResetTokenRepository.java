package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.PasswordResetToken;
import com.example.complaintmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByOtpAndUserAndUsedFalse(String otp, User user);
    void deleteByUser(User user);
}
