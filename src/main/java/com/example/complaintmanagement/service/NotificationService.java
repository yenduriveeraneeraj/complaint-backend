package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.NotificationResponse;
import com.example.complaintmanagement.entity.*;
import com.example.complaintmanagement.exception.ResourceNotFoundException;
import com.example.complaintmanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /** Create a new notification for a user about a complaint */
    public void createNotification(User user, Complaint complaint, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .complaint(complaint)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    /** Get all notifications for a user */
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .complaintId(n.getComplaint() != null ? n.getComplaint().getId() : null)
                        .complaintNumber(n.getComplaint() != null ? n.getComplaint().getComplaintNumber() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /** Mark a notification as read */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /** Mark ALL notifications as read for a user */
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    /** Count unread notifications */
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }
}
