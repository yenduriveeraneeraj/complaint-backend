package com.example.complaintmanagement.dto;

import lombok.*;

/** Dashboard statistics for admin and user dashboards */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStats {
    // For admin dashboard
    private long totalUsers;
    private long totalComplaints;
    private long pendingComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long rejectedComplaints;

    // For user dashboard (only their complaints)
    private long myTotalComplaints;
    private long myPendingComplaints;
    private long myInProgressComplaints;
    private long myResolvedComplaints;
}
