package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.*;
import com.example.complaintmanagement.entity.*;
import com.example.complaintmanagement.exception.*;
import com.example.complaintmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final StaffRepository staffRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketNotificationService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public ComplaintRepository getComplaintRepository() {
        return complaintRepository;
    }

    // ── SUBMIT COMPLAINT ──────────────────────────────────────

    @Transactional
    public ComplaintResponse submitComplaint(
            String title,
            String category,
            String description,
            String priority,
            String location,
            Long departmentId,
            MultipartFile attachment,
            Long userId) {

        if (title == null || title.trim().length() < 3) {
            throw new BadRequestException("Title must be at least 3 characters long.");
        }
        if (description == null || description.trim().length() < 5) {
            throw new BadRequestException("Description must be at least 5 characters long.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Complaint.Category catEnum;
        try {
            catEnum = Complaint.Category.valueOf(category.toUpperCase().trim().replace(" ", "_").replace("/", "_"));
        } catch (Exception e) {
            catEnum = Complaint.Category.OTHER;
        }

        Complaint.Priority prioEnum;
        try {
            prioEnum = Complaint.Priority.valueOf(priority.toUpperCase().trim());
        } catch (Exception e) {
            prioEnum = Complaint.Priority.MEDIUM;
        }

        Department department = null;
        if (departmentId != null) {
            department = departmentRepository.findById(departmentId).orElse(null);
        }

        // Auto-routing if department unassigned
        if (department == null) {
            department = autoAssignDepartment(catEnum);
        }

        String complaintNumber = generateComplaintNumber();

        String attachmentPath = null;
        String attachmentName = null;

        if (attachment != null && !attachment.isEmpty()) {
            try {
                attachmentPath = saveFile(attachment, complaintNumber);
                attachmentName = StringUtils.cleanPath(attachment.getOriginalFilename());
            } catch (IOException e) {
                throw new BadRequestException("Failed to save attachment. Please try again.");
            }
        }

        LocalDateTime slaDeadline = calculateSlaDeadline(prioEnum);

        Complaint complaint = Complaint.builder()
                .complaintNumber(complaintNumber)
                .title(title)
                .description(description)
                .category(catEnum)
                .priority(prioEnum)
                .status(Complaint.Status.PENDING)
                .location(location)
                .department(department)
                .attachmentPath(attachmentPath)
                .attachmentName(attachmentName)
                .slaDeadline(slaDeadline)
                .isOverdue(false)
                .user(user)
                .build();

        complaint = complaintRepository.save(complaint);

        String initialHistoryMsg = "Complaint submitted successfully.";
        if (department != null) {
            initialHistoryMsg += " Auto-routed to " + department.getName() + ".";
        }
        saveHistoryEntry(complaint, Complaint.Status.PENDING, initialHistoryMsg, "System");

        notificationService.createNotification(
                user,
                complaint,
                "Your complaint " + complaintNumber + " has been submitted successfully. " +
                "We will review it shortly."
        );

        webSocketNotificationService.notifyComplaintUpdate(complaintNumber, "CREATED");

        return mapToResponse(complaint);
    }

    // ── AUTO ASSIGNMENT RULE ─────────────────────────────────

    private Department autoAssignDepartment(Complaint.Category category) {
        if (category == null) return null;
        List<Department> depts = departmentRepository.findAll();
        if (depts.isEmpty()) return null;

        String deptKeyword = switch (category) {
            case ELECTRICAL -> "electric";
            case WATER -> "water";
            case INTERNET_WIFI -> "it";
            case CLEANLINESS -> "sanitation";
            case MAINTENANCE, TRANSPORT, SECURITY -> "road";
            default -> null;
        };

        if (deptKeyword == null) return depts.get(0);

        return depts.stream()
                .filter(d -> d.getName().toLowerCase().contains(deptKeyword))
                .findFirst()
                .orElse(depts.get(0));
    }

    // ── SLA DEADLINE CALCULATION ──────────────────────────────

    private LocalDateTime calculateSlaDeadline(Complaint.Priority priority) {
        LocalDateTime now = LocalDateTime.now();
        if (priority == null) return now.plusHours(72);
        return switch (priority) {
            case URGENT -> now.plusHours(24);
            case HIGH -> now.plusHours(48);
            case MEDIUM -> now.plusHours(72);
            case LOW -> now.plusDays(7);
        };
    }

    // ── PUBLIC COMPLAINT TRACKING ────────────────────────────

    public PublicComplaintTrackResponse getPublicComplaintTrack(String complaintNumber) {
        Complaint complaint = complaintRepository.findByComplaintNumber(complaintNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "number", complaintNumber));

        List<StatusHistoryResponse> timeline = getStatusHistory(complaint.getId());

        return PublicComplaintTrackResponse.builder()
                .complaintNumber(complaint.getComplaintNumber())
                .title(complaint.getTitle())
                .category(complaint.getCategory())
                .priority(complaint.getPriority())
                .status(complaint.getStatus())
                .departmentName(complaint.getDepartment() != null ? complaint.getDepartment().getName() : "Unassigned")
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .slaDeadline(complaint.getSlaDeadline())
                .isOverdue(complaint.getIsOverdue())
                .timeline(timeline)
                .build();
    }

    // ── COMPLAINT LISTINGS (Paged & Non-Paged) ───────────────

    public List<ComplaintResponse> getUserComplaints(Long userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<ComplaintResponse> getUserComplaintsPaged(Long userId, Pageable pageable) {
        return complaintRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    public List<ComplaintResponse> getAllComplaints(
            String status, String category, String priority, Long departmentId) {

        Complaint.Status statusEnum = (status != null && !status.isBlank())
                ? Complaint.Status.valueOf(status) : null;
        Complaint.Category categoryEnum = (category != null && !category.isBlank())
                ? Complaint.Category.valueOf(category) : null;
        Complaint.Priority priorityEnum = (priority != null && !priority.isBlank())
                ? Complaint.Priority.valueOf(priority) : null;

        return complaintRepository.findWithFilters(statusEnum, categoryEnum, priorityEnum, departmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<ComplaintResponse> getAllComplaintsPaged(
            String status, String category, String priority, Long departmentId, Pageable pageable) {

        Complaint.Status statusEnum = (status != null && !status.isBlank())
                ? Complaint.Status.valueOf(status) : null;
        Complaint.Category categoryEnum = (category != null && !category.isBlank())
                ? Complaint.Category.valueOf(category) : null;
        Complaint.Priority priorityEnum = (priority != null && !priority.isBlank())
                ? Complaint.Priority.valueOf(priority) : null;

        return complaintRepository.findWithFiltersPaged(statusEnum, categoryEnum, priorityEnum, departmentId, pageable)
                .map(this::mapToResponse);
    }

    public Page<ComplaintResponse> getStaffComplaintsPaged(Long staffId, Pageable pageable) {
        return complaintRepository.findByAssignedStaffId(staffId, pageable)
                .map(this::mapToResponse);
    }

    public List<ComplaintResponse> getStaffComplaints(Long staffId) {
        return complaintRepository.findByAssignedStaffIdOrderByCreatedAtDesc(staffId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── GET COMPLAINT DETAILS ─────────────────────────────────

    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", id));
        return mapToResponse(complaint);
    }

    // ── GET STATUS HISTORY ────────────────────────────────────

    public List<StatusHistoryResponse> getStatusHistory(Long complaintId) {
        return historyRepository.findByComplaintIdOrderByChangedAtAsc(complaintId)
                .stream()
                .map(h -> StatusHistoryResponse.builder()
                        .id(h.getId())
                        .status(h.getStatus())
                        .message(h.getMessage())
                        .changedBy(h.getChangedBy())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ── ASSIGN COMPLAINT (Admin) ──────────────────────────────

    @Transactional
    public ComplaintResponse assignComplaint(Long complaintId, AssignComplaintRequest req,
                                              String adminName) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", complaintId));

        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", req.getDepartmentId()));

        complaint.setDepartment(department);
        String historyMsg = "Complaint assigned to " + department.getName() + ".";

        if (req.getStaffId() != null) {
            Staff staff = staffRepository.findById(req.getStaffId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", req.getStaffId()));
            complaint.setAssignedStaff(staff);
            historyMsg += " Staff member " + staff.getName() + " has been assigned.";
        }

        if (req.getRemarks() != null && !req.getRemarks().isBlank()) {
            complaint.setRemarks(req.getRemarks());
        }

        complaint = complaintRepository.save(complaint);

        saveHistoryEntry(complaint, complaint.getStatus(), historyMsg, adminName);

        notificationService.createNotification(
                complaint.getUser(),
                complaint,
                "Your complaint " + complaint.getComplaintNumber() +
                " has been assigned to " + department.getName() + "."
        );

        webSocketNotificationService.notifyComplaintUpdate(complaint.getComplaintNumber(), "ASSIGNED");

        return mapToResponse(complaint);
    }

    // ── UPDATE STATUS (Admin / Staff) ─────────────────────────

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId, UpdateStatusRequest req,
                                           String actorName) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", complaintId));

        Complaint.Status oldStatus = complaint.getStatus();
        complaint.setStatus(req.getStatus());

        if (req.getRemarks() != null && !req.getRemarks().isBlank()) {
            complaint.setRemarks(req.getRemarks());
        }

        if (req.getStatus() == Complaint.Status.RESOLVED ||
            req.getStatus() == Complaint.Status.REJECTED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        if (req.getStaffId() != null) {
            Staff staff = staffRepository.findById(req.getStaffId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", req.getStaffId()));
            complaint.setAssignedStaff(staff);
        }

        complaint = complaintRepository.save(complaint);

        String historyMsg = req.getRemarks() != null ? req.getRemarks()
                : "Status updated from " + oldStatus + " to " + req.getStatus() + ".";
        saveHistoryEntry(complaint, req.getStatus(), historyMsg, actorName);

        notificationService.createNotification(
                complaint.getUser(),
                complaint,
                "Your complaint " + complaint.getComplaintNumber() +
                " status has been updated to: " + req.getStatus().name().replace("_", " ")
        );

        if (req.getStatus() == Complaint.Status.RESOLVED) {
            emailService.sendComplaintResolvedEmail(complaint.getUser(), complaint, req.getRemarks());
        } else {
            emailService.sendComplaintStatusUpdateEmail(
                complaint.getUser(),
                complaint,
                req.getStatus().name().replace("_", " "),
                req.getRemarks()
            );
        }

        webSocketNotificationService.notifyComplaintUpdate(complaint.getComplaintNumber(), "STATUS_UPDATED");

        return mapToResponse(complaint);
    }

    // ── DASHBOARD STATS ───────────────────────────────────────

    public DashboardStats getUserDashboardStats(Long userId) {
        return DashboardStats.builder()
                .myTotalComplaints(complaintRepository.countByUserId(userId))
                .myPendingComplaints(complaintRepository.countByUserIdAndStatus(userId, Complaint.Status.PENDING))
                .myInProgressComplaints(complaintRepository.countByUserIdAndStatus(userId, Complaint.Status.IN_PROGRESS))
                .myResolvedComplaints(complaintRepository.countByUserIdAndStatus(userId, Complaint.Status.RESOLVED))
                .build();
    }

    public DashboardStats getAdminDashboardStats() {
        return DashboardStats.builder()
                .totalUsers(userRepository.countByRole(User.Role.ROLE_USER))
                .totalComplaints(complaintRepository.count())
                .pendingComplaints(complaintRepository.countByStatus(Complaint.Status.PENDING))
                .inProgressComplaints(complaintRepository.countByStatus(Complaint.Status.IN_PROGRESS))
                .resolvedComplaints(complaintRepository.countByStatus(Complaint.Status.RESOLVED))
                .rejectedComplaints(complaintRepository.countByStatus(Complaint.Status.REJECTED))
                .build();
    }

    public DashboardStats getStaffDashboardStats(Long staffId) {
        return DashboardStats.builder()
                .myTotalComplaints(complaintRepository.countByAssignedStaffId(staffId))
                .myPendingComplaints(complaintRepository.countByAssignedStaffIdAndStatus(staffId, Complaint.Status.PENDING))
                .myInProgressComplaints(complaintRepository.countByAssignedStaffIdAndStatus(staffId, Complaint.Status.IN_PROGRESS))
                .myResolvedComplaints(complaintRepository.countByAssignedStaffIdAndStatus(staffId, Complaint.Status.RESOLVED))
                .build();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────

    private String generateComplaintNumber() {
        int year = Year.now().getValue();
        List<String> existing = complaintRepository.findLatestComplaintNumbers();

        int nextSeq = 1;
        if (!existing.isEmpty()) {
            String last = existing.get(0);
            try {
                String seqPart = last.substring(last.lastIndexOf('-') + 1);
                nextSeq = Integer.parseInt(seqPart) + 1;
            } catch (Exception ignored) {}
        }

        return String.format("CMP-%d-%05d", year, nextSeq);
    }

    private String saveFile(MultipartFile file, String complaintNumber) throws IOException {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        List<String> allowed = List.of("jpg", "jpeg", "png", "pdf");

        if (!allowed.contains(extension)) {
            throw new BadRequestException("Only JPG, JPEG, PNG, and PDF files are allowed.");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String fileName = complaintNumber + "_" + originalName;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private void saveHistoryEntry(Complaint complaint, Complaint.Status status,
                                   String message, String changedBy) {
        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(complaint)
                .status(status)
                .message(message)
                .changedBy(changedBy)
                .build();
        historyRepository.save(history);
    }

    public ComplaintResponse mapToResponse(Complaint c) {
        ComplaintResponse resp = new ComplaintResponse();
        resp.setId(c.getId());
        resp.setComplaintNumber(c.getComplaintNumber());
        resp.setTitle(c.getTitle());
        resp.setDescription(c.getDescription());
        resp.setCategory(c.getCategory());
        resp.setPriority(c.getPriority());
        resp.setStatus(c.getStatus());
        resp.setLocation(c.getLocation());
        resp.setRemarks(c.getRemarks());
        resp.setAttachmentName(c.getAttachmentName());
        resp.setAttachmentPath(c.getAttachmentPath());
        resp.setSlaDeadline(c.getSlaDeadline());
        resp.setIsOverdue(c.getIsOverdue() != null ? c.getIsOverdue() : false);
        resp.setCreatedAt(c.getCreatedAt());
        resp.setUpdatedAt(c.getUpdatedAt());
        resp.setResolvedAt(c.getResolvedAt());

        if (c.getUser() != null) {
            resp.setUserId(c.getUser().getId());
            resp.setUserName(c.getUser().getFullName());
            resp.setUserEmail(c.getUser().getEmail());
        }
        if (c.getDepartment() != null) {
            resp.setDepartmentId(c.getDepartment().getId());
            resp.setDepartmentName(c.getDepartment().getName());
        }
        if (c.getAssignedStaff() != null) {
            resp.setStaffId(c.getAssignedStaff().getId());
            resp.setStaffName(c.getAssignedStaff().getName());
        }
        return resp;
    }
}
