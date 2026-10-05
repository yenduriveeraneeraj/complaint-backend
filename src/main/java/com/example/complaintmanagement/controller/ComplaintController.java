package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ComplaintController — user-facing complaint APIs
 *
 * GET  /api/complaints          → user's own complaints
 * POST /api/complaints          → submit new complaint
 * GET  /api/complaints/{id}     → complaint details
 * GET  /api/complaints/{id}/history → status history (timeline)
 * GET  /api/user/dashboard      → user dashboard stats
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserService userService;
    private final NotificationService notificationService;

    /** Get logged-in user's ID from Spring Security context */
    private Long getCurrentUserId(Authentication auth) {
        User user = userService.getUserByEmail(auth.getName());
        return user.getId();
    }

    // ── USER DASHBOARD ─────────────────────────────────────────

    @GetMapping("/user/dashboard")
    public ResponseEntity<DashboardStats> getUserDashboard(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return ResponseEntity.ok(complaintService.getUserDashboardStats(userId));
    }

    // ── SUBMIT COMPLAINT ───────────────────────────────────────

    /**
     * POST /api/complaints
     *
     * Uses multipart/form-data because file attachment is included.
     * React sends this as FormData, NOT JSON.
     *
     * @RequestParam extracts individual form fields
     * @RequestPart extracts the file
     */
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

    // ── MY COMPLAINTS ──────────────────────────────────────────

    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintResponse>> getMyComplaints(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return ResponseEntity.ok(complaintService.getUserComplaints(userId));
    }

    // ── COMPLAINT DETAILS ──────────────────────────────────────

    @GetMapping("/complaints/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
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
