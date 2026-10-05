package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.*;
import com.example.complaintmanagement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminController — all admin-only APIs.
 * All routes are secured: ROLE_ADMIN required (enforced in SecurityConfig).
 *
 * GET  /api/admin/dashboard          → admin stats
 * GET  /api/admin/complaints         → all complaints with filters
 * PUT  /api/admin/complaints/{id}/assign  → assign to dept/staff
 * PUT  /api/admin/complaints/{id}/status  → update status
 * GET  /api/admin/users              → all users
 * PUT  /api/admin/users/{id}/toggle  → activate/deactivate user
 * GET  /api/departments              → list departments
 * POST /api/departments              → add department
 * PUT  /api/departments/{id}         → edit department
 * PUT  /api/departments/{id}/toggle  → activate/deactivate
 * GET  /api/staff                    → list staff
 * POST /api/staff                    → add staff
 * PUT  /api/staff/{id}               → edit staff
 * PUT  /api/staff/{id}/toggle        → activate/deactivate
 * GET  /api/admin/feedback           → all feedback
 * GET  /api/admin/reports/charts     → chart data
 */
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final ComplaintService complaintService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final StaffService staffService;
    private final FeedbackService feedbackService;

    // ══ ADMIN DASHBOARD ═════════════════════════════════════

    @GetMapping("/api/admin/dashboard")
    public ResponseEntity<DashboardStats> getAdminDashboard() {
        return ResponseEntity.ok(complaintService.getAdminDashboardStats());
    }

    // ══ COMPLAINT MANAGEMENT ════════════════════════════════

    @GetMapping("/api/admin/complaints")
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(
            complaintService.getAllComplaints(status, category, priority, departmentId)
        );
    }

    @PutMapping("/api/admin/complaints/{id}/assign")
    public ResponseEntity<ComplaintResponse> assignComplaint(
            @PathVariable Long id,
            @RequestBody AssignComplaintRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
            complaintService.assignComplaint(id, request, auth.getName())
        );
    }

    @PutMapping("/api/admin/complaints/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
            complaintService.updateStatus(id, request, auth.getName())
        );
    }

    // ══ USER MANAGEMENT ═════════════════════════════════════

    @GetMapping("/api/admin/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
            @RequestParam(required = false) String search) {
        List<User> users = (search != null && !search.isBlank())
                ? userService.searchUsers(search)
                : userService.getAllUsers();

        List<Map<String, Object>> result = users.stream()
                .filter(u -> u.getRole() == User.Role.ROLE_USER)
                .map(u -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", u.getId());
                    map.put("fullName", u.getFullName());
                    map.put("email", u.getEmail());
                    map.put("phone", u.getPhone());
                    map.put("isActive", u.getIsActive());
                    map.put("createdAt", u.getCreatedAt());
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/api/admin/users/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        User u = userService.toggleUserStatus(id);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("fullName", u.getFullName());
        map.put("isActive", u.getIsActive());
        map.put("message", u.getIsActive() ? "User activated." : "User deactivated.");
        return ResponseEntity.ok(map);
    }

    // ══ DEPARTMENT MANAGEMENT ═══════════════════════════════

    @GetMapping("/api/departments")
    public ResponseEntity<List<Department>> getDepartments(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(activeOnly
                ? departmentService.getAllActive()
                : departmentService.getAll());
    }

    @PostMapping("/api/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            departmentService.create(body.get("name"), body.get("description"))
        );
    }

    @PutMapping("/api/departments/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
            departmentService.update(id, body.get("name"), body.get("description"))
        );
    }

    @PutMapping("/api/departments/{id}/toggle")
    public ResponseEntity<Department> toggleDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.toggleStatus(id));
    }

    // ══ STAFF MANAGEMENT ════════════════════════════════════

    @GetMapping("/api/staff")
    public ResponseEntity<List<Staff>> getStaff(
            @RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(staffService.getStaffByDepartment(departmentId));
        }
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @PostMapping("/api/staff")
    public ResponseEntity<Staff> createStaff(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            staffService.create(
                (String) body.get("name"),
                (String) body.get("email"),
                (String) body.get("phone"),
                (String) body.get("designation"),
                Long.valueOf(body.get("departmentId").toString())
            )
        );
    }

    @PutMapping("/api/staff/{id}")
    public ResponseEntity<Staff> updateStaff(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
            staffService.update(
                id,
                (String) body.get("name"),
                (String) body.get("phone"),
                (String) body.get("designation"),
                Long.valueOf(body.get("departmentId").toString())
            )
        );
    }

    @PutMapping("/api/staff/{id}/toggle")
    public ResponseEntity<Staff> toggleStaff(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.toggleStatus(id));
    }

    // ══ FEEDBACK MANAGEMENT ═════════════════════════════════

    @GetMapping("/api/admin/feedback")
    public ResponseEntity<Map<String, Object>> getAllFeedback() {
        List<FeedbackResponse> feedbacks = feedbackService.getAllFeedback();
        Double avgRating = feedbackService.getAverageRating();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("feedbacks", feedbacks);
        result.put("totalFeedback", feedbacks.size());
        result.put("averageRating", avgRating);
        return ResponseEntity.ok(result);
    }

    // ══ REPORTS / CHART DATA ════════════════════════════════

    @GetMapping("/api/admin/reports/charts")
    public ResponseEntity<Map<String, Object>> getChartData() {
        Map<String, Object> charts = new LinkedHashMap<>();

        // Complaints by status
        List<Object[]> byStatus = complaintService.getComplaintRepository()
                .countByStatusGrouped();
        charts.put("byStatus", byStatus.stream().map(row ->
            Map.of("name", row[0].toString(), "value", ((Number) row[1]).longValue())
        ).collect(Collectors.toList()));

        // Complaints by category
        List<Object[]> byCategory = complaintService.getComplaintRepository()
                .countByCategory();
        charts.put("byCategory", byCategory.stream().map(row ->
            Map.of("name", row[0].toString(), "value", ((Number) row[1]).longValue())
        ).collect(Collectors.toList()));

        // Complaints by priority
        List<Object[]> byPriority = complaintService.getComplaintRepository()
                .countByPriority();
        charts.put("byPriority", byPriority.stream().map(row ->
            Map.of("name", row[0].toString(), "value", ((Number) row[1]).longValue())
        ).collect(Collectors.toList()));

        return ResponseEntity.ok(charts);
    }
}
