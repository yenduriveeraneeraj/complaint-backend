package com.example.complaintmanagement.controller;

import com.example.complaintmanagement.dto.ComplaintResponse;
import com.example.complaintmanagement.dto.PublicComplaintTrackResponse;
import com.example.complaintmanagement.entity.Complaint;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.security.JwtAuthFilter;
import com.example.complaintmanagement.security.RateLimitingFilter;
import com.example.complaintmanagement.service.ComplaintService;
import com.example.complaintmanagement.service.NotificationService;
import com.example.complaintmanagement.service.ReportExportService;
import com.example.complaintmanagement.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ComplaintController.class, PublicComplaintController.class})
@AutoConfigureMockMvc(addFilters = false)
class ComplaintControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComplaintService complaintService;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private ReportExportService reportExportService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @Test
    @DisplayName("GET /api/complaints should return list of user complaints when authenticated")
    @WithMockUser(username = "user@cms.com", roles = {"USER"})
    void testGetMyComplaints() throws Exception {
        User user = User.builder().id(1L).email("user@cms.com").role(User.Role.ROLE_USER).build();
        when(userService.getUserByEmail(any())).thenReturn(user);

        ComplaintResponse response = ComplaintResponse.builder()
                .id(1L)
                .complaintNumber("CMP-2026-00001")
                .title("Water Supply Leakage")
                .category(Complaint.Category.WATER)
                .priority(Complaint.Priority.URGENT)
                .status(Complaint.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .isOverdue(false)
                .build();

        when(complaintService.getUserComplaints(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].complaintNumber").value("CMP-2026-00001"))
                .andExpect(jsonPath("$[0].title").value("Water Supply Leakage"));
    }

    @Test
    @DisplayName("GET /api/public/complaints/track/{complaintNumber} should return public tracking info without authentication")
    void testPublicTrackingEndpoint() throws Exception {
        PublicComplaintTrackResponse publicResponse = PublicComplaintTrackResponse.builder()
                .complaintNumber("CMP-2026-00001")
                .title("Public Road Pothole")
                .category(Complaint.Category.MAINTENANCE)
                .priority(Complaint.Priority.MEDIUM)
                .status(Complaint.Status.IN_PROGRESS)
                .departmentName("Roads & Infrastructure")
                .createdAt(LocalDateTime.now())
                .isOverdue(false)
                .timeline(List.of())
                .build();

        when(complaintService.getPublicComplaintTrack("CMP-2026-00001")).thenReturn(publicResponse);

        mockMvc.perform(get("/api/public/complaints/track/CMP-2026-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complaintNumber").value("CMP-2026-00001"))
                .andExpect(jsonPath("$.departmentName").value("Roads & Infrastructure"));
    }
}
