package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.CommentRequest;
import com.example.complaintmanagement.dto.CommentResponse;
import com.example.complaintmanagement.entity.Complaint;
import com.example.complaintmanagement.entity.ComplaintComment;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.exception.ResourceNotFoundException;
import com.example.complaintmanagement.repository.ComplaintCommentRepository;
import com.example.complaintmanagement.repository.ComplaintRepository;
import com.example.complaintmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintCommentService {

    private final ComplaintCommentRepository commentRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketNotificationService;

    public List<CommentResponse> getComments(Long complaintId) {
        if (!complaintRepository.existsById(complaintId)) {
            throw new ResourceNotFoundException("Complaint", complaintId);
        }
        return commentRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(Long complaintId, Long authorUserId, CommentRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", complaintId));
        User author = userRepository.findById(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", authorUserId));

        ComplaintComment comment = ComplaintComment.builder()
                .complaint(complaint)
                .authorUser(author)
                .commentText(request.getCommentText())
                .build();

        comment = commentRepository.save(comment);

        // Notify complaint owner if author is staff/admin, or notify assigned staff/admin if author is citizen
        if (!complaint.getUser().getId().equals(author.getId())) {
            notificationService.createNotification(
                    complaint.getUser(),
                    complaint,
                    "New message on your complaint " + complaint.getComplaintNumber() + ": " + author.getFullName()
            );
        }

        // Push real-time WS notification
        webSocketNotificationService.notifyComplaintUpdate(complaint.getComplaintNumber(), "NEW_COMMENT");

        return mapToResponse(comment);
    }

    private CommentResponse mapToResponse(ComplaintComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .authorUserId(comment.getAuthorUser().getId())
                .authorName(comment.getAuthorUser().getFullName())
                .authorRole(comment.getAuthorUser().getRole().name())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
