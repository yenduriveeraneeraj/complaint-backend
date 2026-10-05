package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.*;
import com.example.complaintmanagement.exception.*;
import com.example.complaintmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    /** Submit feedback for a resolved complaint */
    public FeedbackResponse submitFeedback(FeedbackRequest request, Long userId) {
        // Check complaint exists
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", request.getComplaintId()));

        // Only RESOLVED complaints can receive feedback
        if (complaint.getStatus() != Complaint.Status.RESOLVED) {
            throw new BadRequestException(
                "Feedback can only be submitted after the complaint is resolved."
            );
        }

        // Check the complaint belongs to this user
        if (!complaint.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only give feedback for your own complaints.");
        }

        // Check feedback not already submitted
        if (feedbackRepository.existsByComplaintId(request.getComplaintId())) {
            throw new BadRequestException("Feedback has already been submitted for this complaint.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Feedback feedback = Feedback.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .complaint(complaint)
                .build();

        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    /** Get all feedback (admin view) */
    public List<FeedbackResponse> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /** Get average rating */
    public Double getAverageRating() {
        Double avg = feedbackRepository.findAverageRating();
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    private FeedbackResponse mapToResponse(Feedback f) {
        return FeedbackResponse.builder()
                .id(f.getId())
                .rating(f.getRating())
                .comment(f.getComment())
                .createdAt(f.getCreatedAt())
                .userId(f.getUser().getId())
                .userName(f.getUser().getFullName())
                .complaintId(f.getComplaint().getId())
                .complaintNumber(f.getComplaint().getComplaintNumber())
                .build();
    }
}
