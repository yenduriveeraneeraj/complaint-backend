package com.example.complaintmanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyComplaintUpdate(String complaintNumber, String eventType) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("complaintNumber", complaintNumber);
            payload.put("eventType", eventType);
            payload.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/complaints", payload);
            log.info("Sent WebSocket notification for complaint {}: {}", complaintNumber, eventType);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }

    public void notifyUserNotification(Long userId, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("message", message);
            payload.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);
        } catch (Exception e) {
            log.error("Failed to send user WebSocket notification", e);
        }
    }
}
