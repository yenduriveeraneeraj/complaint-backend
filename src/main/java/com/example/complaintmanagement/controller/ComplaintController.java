package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ReportExportService reportExportService;

    private Long getCurrentUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        User user = userService.getUserByEmail(auth.getName());
        if (user == null) {
            return 1L; // fallback default
        }
        return user.getId();
    }

    // ── USER DASHBOARD ─────────────────────────────────────────

    @GetMapping("/user/dashboard")
    public ResponseEntity<DashboardStats> getUserDashboard(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return ResponseEntity.ok(complaintService.getUserDashboardStats(userId));
    }

    // ── SUBMIT COMPLAINT ───────────────────────────────────────

    @PostMapping(value = "/complaints", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintResponse> submitComplaint(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("priority") String priority,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            Authentication auth) {

        MultipartFile finalAttachment = (file != null && !file.isEmpty()) ? file : attachment;

        Long userId = getCurrentUserId(auth);
        ComplaintResponse response = complaintService.submitComplaint(
                title, category, description, priority, location, departmentId, finalAttachment, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── MY COMPLAINTS (Paged & Non-Paged) ──────────────────────

    @GetMapping("/complaints")
    public ResponseEntity<?> getMyComplaints(
            Authentication auth,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        Long userId = getCurrentUserId(auth);

        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ComplaintResponse> pagedResult = complaintService.getUserComplaintsPaged(userId, pageable);
            return ResponseEntity.ok(pagedResult);
        } else {
            List<ComplaintResponse> listResult = complaintService.getUserComplaints(userId);
            return ResponseEntity.ok(listResult);
        }
    }

    // ── COMPLAINT DETAILS ──────────────────────────────────────

    @GetMapping("/complaints/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    // ── DOWNLOAD RECEIPT PDF ────────────────────────────────────

    @GetMapping("/complaints/{id}/receipt")
    public void downloadReceipt(@PathVariable Long id, HttpServletResponse response) throws IOException {
        ComplaintResponse complaint = complaintService.getComplaintById(id);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=receipt-" + complaint.getComplaintNumber() + ".pdf");
        reportExportService.generateReceiptPdf(response.getOutputStream(), complaint);
    }

    // ── STATUS HISTORY (Timeline) ──────────────────────────────

    @GetMapping("/complaints/{id}/history")
    public ResponseEntity<List<StatusHistoryResponse>> getStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getStatusHistory(id));
    }

    // ── NOTIFICATIONS ──────────────────────────────────────────

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication auth) {
        notificationService.markAllAsRead(getCurrentUserId(auth));
        return ResponseEntity.ok().build();
    }
}
