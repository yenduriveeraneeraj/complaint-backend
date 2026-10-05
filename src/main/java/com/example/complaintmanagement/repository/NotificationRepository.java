package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Get all notifications for a user — newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    // Get only unread notifications for a user
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);
    // Count unread notifications
    long countByUserIdAndIsRead(Long userId, Boolean isRead);
}
