package com.example.complaintmanagement.service;

import com.example.complaintmanagement.entity.Complaint;
import com.example.complaintmanagement.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * EmailService — Sends automated Gmail / SMTP email notifications to citizens
 * when their complaint work is completed or status updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:resolver.cms.notifications@gmail.com}")
    private String fromEmail;

    /**
     * Send email notification to citizen when complaint work is marked RESOLVED.
     */
    @Async
    public void sendComplaintResolvedEmail(User citizen, Complaint complaint, String remarks) {
        if (citizen == null || citizen.getEmail() == null || citizen.getEmail().isBlank()) {
            log.warn("Cannot send completion email: Citizen email is null or empty");
            return;
        }

        String subject = "✅ Complaint Resolved: [" + complaint.getComplaintNumber() + "] " + complaint.getTitle();
        String body = "Dear " + citizen.getFullName() + ",\n\n"
                + "We are pleased to inform you that your complaint work has been successfully COMPLETED and RESOLVED.\n\n"
                + "--------------------------------------------------\n"
                + "Complaint ID     : " + complaint.getComplaintNumber() + "\n"
                + "Title            : " + complaint.getTitle() + "\n"
                + "Category         : " + complaint.getCategory() + "\n"
                + "Location         : " + (complaint.getLocation() != null ? complaint.getLocation() : "N/A") + "\n"
                + "Official Remarks : " + (remarks != null && !remarks.isBlank() ? remarks : "Work completed successfully by assigned department.") + "\n"
                + "--------------------------------------------------\n\n"
                + "You can view complete resolution history or provide star rating feedback on your dashboard:\n"
                + "http://localhost:5173/user/dashboard\n\n"
                + "Thank you for using the Online Complaint Management System (ResolverCMS).\n\n"
                + "Best regards,\n"
                + "ResolverCMS Administration Team";

        sendEmail(citizen.getEmail(), subject, body);
    }

    /**
     * Send general status update email notification to citizen.
     */
    @Async
    public void sendComplaintStatusUpdateEmail(User citizen, Complaint complaint, String statusName, String remarks) {
        if (citizen == null || citizen.getEmail() == null || citizen.getEmail().isBlank()) {
            return;
        }

        String subject = "🔔 Status Update: [" + complaint.getComplaintNumber() + "] " + complaint.getTitle();
        String body = "Dear " + citizen.getFullName() + ",\n\n"
                + "Your complaint [" + complaint.getComplaintNumber() + "] status has been updated to: " + statusName + ".\n\n"
                + "--------------------------------------------------\n"
                + "Complaint ID : " + complaint.getComplaintNumber() + "\n"
                + "Title        : " + complaint.getTitle() + "\n"
                + "New Status   : " + statusName + "\n"
                + "Remarks      : " + (remarks != null && !remarks.isBlank() ? remarks : "Updated by administrator.") + "\n"
                + "--------------------------------------------------\n\n"
                + "Track progress on your dashboard: http://localhost:5173/user/dashboard\n\n"
                + "Best regards,\n"
                + "ResolverCMS Support Team";

        sendEmail(citizen.getEmail(), subject, body);
    }

    private void sendEmail(String toEmail, String subject, String text) {
        log.info("📧 Sending Gmail notification to: {} | Subject: {}", toEmail, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("✅ Gmail email notification sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("⚠️ Note: Email notification dispatch to {} logged cleanly (SMTP setup: {}): {}", toEmail, fromEmail, e.getMessage());
        }
    }
}
