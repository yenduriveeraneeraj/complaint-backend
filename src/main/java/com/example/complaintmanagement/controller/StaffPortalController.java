package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.Staff;
import com.example.complaintmanagement.exception.ResourceNotFoundException;
import com.example.complaintmanagement.repository.StaffRepository;
import com.example.complaintmanagement.security.UserPrincipal;
import com.example.complaintmanagement.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff-portal")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffPortalController {

    private final ComplaintService complaintService;
    private final StaffRepository staffRepository;

    private Staff getStaffForUser(UserPrincipal userPrincipal) {
        return staffRepository.findByUserId(userPrincipal.getId())
                .or(() -> staffRepository.findByEmail(userPrincipal.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("Staff record for logged in user", userPrincipal.getId()));
    }

    @GetMapping("/my-complaints")
    public ResponseEntity<?> getMyComplaints(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        Staff staff = getStaffForUser(userPrincipal);

        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ComplaintResponse> complaints = complaintService.getStaffComplaintsPaged(staff.getId(), pageable);
            return ResponseEntity.ok(complaints);
        } else {
            List<ComplaintResponse> complaints = complaintService.getStaffComplaints(staff.getId());
            return ResponseEntity.ok(complaints);
        }
    }

    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ComplaintResponse updated = complaintService.updateStatus(id, request, userPrincipal.getFullName() + " (Staff)");
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Staff staff = getStaffForUser(userPrincipal);
        DashboardStats stats = complaintService.getStaffDashboardStats(staff.getId());
        return ResponseEntity.ok(stats);
    }
}
