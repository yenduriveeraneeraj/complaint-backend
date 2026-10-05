package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // Check if feedback already exists for a complaint
    boolean existsByComplaintId(Long complaintId);
    // Find feedback by complaint
    Optional<Feedback> findByComplaintId(Long complaintId);
    // Get all feedback ordered by newest first (admin view)
    List<Feedback> findAllByOrderByCreatedAtDesc();
    // Calculate average rating
    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double findAverageRating();
}
