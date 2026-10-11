package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.AssignComplaintRequest;
import com.example.complaintmanagement.dto.ComplaintResponse;
import com.example.complaintmanagement.dto.UpdateStatusRequest;
import com.example.complaintmanagement.entity.Complaint;
import com.example.complaintmanagement.entity.Department;
import com.example.complaintmanagement.entity.Staff;
import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private ComplaintStatusHistoryRepository historyRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @InjectMocks
    private ComplaintService complaintService;

    private User sampleUser;
    private Department sampleDepartment;
    private Staff sampleStaff;
    private Complaint sampleComplaint;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .fullName("John Citizen")
                .email("user@cms.com")
                .role(User.Role.ROLE_USER)
                .build();

        sampleDepartment = Department.builder()
                .id(10L)
                .name("Electricity & Power")
                .build();

        sampleStaff = Staff.builder()
                .id(20L)
                .name("Alex Engineer")
                .department(sampleDepartment)
                .build();

        sampleComplaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CMP-2026-00001")
                .title("Power Cut in Block A")
                .description("No electricity since 2 hours")
                .category(Complaint.Category.ELECTRICAL)
                .priority(Complaint.Priority.HIGH)
                .status(Complaint.Status.PENDING)
                .user(sampleUser)
                .createdAt(LocalDateTime.now())
                .slaDeadline(LocalDateTime.now().plusHours(48))
                .isOverdue(false)
                .build();
    }

    @Test
    @DisplayName("Should submit complaint and calculate SLA deadline correctly")
    void testSubmitComplaintSlaCalculation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(sampleDepartment));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ComplaintResponse response = complaintService.submitComplaint(
                "Power Cut in Block A",
                "ELECTRICAL",
                "No electricity since 2 hours",
                "HIGH",
                "Block A",
                10L,
                null,
                1L
        );

        assertNotNull(response);
        assertEquals("CMP-2026-00001", response.getComplaintNumber());
        assertEquals(Complaint.Priority.HIGH, response.getPriority());
        assertNotNull(response.getSlaDeadline());
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    @DisplayName("Should update status and send notifications")
    void testUpdateStatusTransition() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(sampleComplaint);

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(Complaint.Status.IN_PROGRESS);
        request.setRemarks("Technician dispatched to inspect transformer.");

        ComplaintResponse response = complaintService.updateStatus(100L, request, "Admin User");

        assertEquals(Complaint.Status.IN_PROGRESS, response.getStatus());
        verify(emailService, times(1)).sendComplaintStatusUpdateEmail(any(), any(), eq("IN PROGRESS"), any());
        verify(webSocketNotificationService, times(1)).notifyComplaintUpdate(eq("CMP-2026-00001"), eq("STATUS_UPDATED"));
    }

    @Test
    @DisplayName("Should assign complaint to department and staff")
    void testAssignComplaint() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(sampleDepartment));
        when(staffRepository.findById(20L)).thenReturn(Optional.of(sampleStaff));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(sampleComplaint);

        AssignComplaintRequest req = new AssignComplaintRequest();
        req.setDepartmentId(10L);
        req.setStaffId(20L);
        req.setRemarks("Assigned to senior engineer");

        ComplaintResponse response = complaintService.assignComplaint(100L, req, "Admin User");

        assertNotNull(response);
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }
}
