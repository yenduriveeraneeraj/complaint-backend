package com.example.complaintmanagement.service;

import com.example.complaintmanagement.entity.Complaint;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.repository.ComplaintRepository;
import com.example.complaintmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaEscalationScheduler {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * Runs every hour at top of the hour to check for breached SLA deadlines.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkForBreachedSla() {
        log.info("Running SLA Escalation Engine scan...");
        LocalDateTime now = LocalDateTime.now();
        List<Complaint> overdueComplaints = complaintRepository.findPendingBreachedSlaComplaints(now);

        if (overdueComplaints.isEmpty()) {
            log.info("SLA Escalation Engine: No breached tickets found.");
            return;
        }

        log.warn("SLA Escalation Engine: Found {} breached complaints!", overdueComplaints.size());
        List<User> admins = userRepository.findByRole(User.Role.ROLE_ADMIN);

        for (Complaint c : overdueComplaints) {
            c.setIsOverdue(true);
            complaintRepository.save(c);

            // Notify user
            notificationService.createNotification(
                    c.getUser(),
                    c,
                    "CRITICAL: SLA for complaint " + c.getComplaintNumber() + " is overdue! High-priority escalation triggered."
            );

            // Notify all admins
            for (User admin : admins) {
                notificationService.createNotification(
                        admin,
                        c,
                        "ESCALATION ALERT: Complaint " + c.getComplaintNumber() + " has breached SLA deadline (" + c.getPriority() + " priority)."
                );
            }

            webSocketNotificationService.notifyComplaintUpdate(c.getComplaintNumber(), "SLA_BREACHED");
        }
    }
}
