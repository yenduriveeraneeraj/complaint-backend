package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Find all complaints by a specific user
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find paged complaints by user
    Page<Complaint> findByUserId(Long userId, Pageable pageable);

    // Find complaint by its unique complaint number (e.g., CMP-2026-00001)
    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    // Count complaints by status — for dashboard stats
    long countByStatus(Complaint.Status status);

    // Count complaints by user ID and status — for user dashboard
    long countByUserIdAndStatus(Long userId, Complaint.Status status);

    // Count complaints for a specific user
    long countByUserId(Long userId);

    // Count complaints assigned to staff
    long countByAssignedStaffId(Long staffId);
    long countByAssignedStaffIdAndStatus(Long staffId, Complaint.Status status);

    // Find complaints assigned to staff
    List<Complaint> findByAssignedStaffIdOrderByCreatedAtDesc(Long staffId);
    Page<Complaint> findByAssignedStaffId(Long staffId, Pageable pageable);

    // Find complaints filtered by status
    List<Complaint> findByStatusOrderByCreatedAtDesc(Complaint.Status status);

    // Find complaints by department
    List<Complaint> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    // Find complaints by category
    List<Complaint> findByCategoryOrderByCreatedAtDesc(Complaint.Category category);

    // Find the latest complaint number to generate the next one
    @Query("SELECT c.complaintNumber FROM Complaint c ORDER BY c.id DESC")
    List<String> findLatestComplaintNumbers();

    // Admin: search complaints by keyword in title or number
    @Query("SELECT c FROM Complaint c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.complaintNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Complaint> searchComplaints(@Param("keyword") String keyword);

    // Reports: complaints within a date range
    @Query("SELECT c FROM Complaint c WHERE c.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.createdAt DESC")
    List<Complaint> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Count complaints grouped by category — for chart
    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countByCategory();

    // Count complaints grouped by status — for chart
    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countByStatusGrouped();

    // Count complaints grouped by priority — for chart
    @Query("SELECT c.priority, COUNT(c) FROM Complaint c GROUP BY c.priority")
    List<Object[]> countByPriority();

    // Monthly complaint count for the past 12 months — for line chart
    @Query(value = "SELECT MONTH(created_at) as month, YEAR(created_at) as year, COUNT(*) as count " +
                   "FROM complaints WHERE created_at >= :startDate " +
                   "GROUP BY YEAR(created_at), MONTH(created_at) " +
                   "ORDER BY year ASC, month ASC",
           nativeQuery = true)
    List<Object[]> countMonthlyComplaints(@Param("startDate") LocalDateTime startDate);

    // Find all complaints with multiple filters (non-paged)
    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:departmentId IS NULL OR c.department.id = :departmentId) " +
           "ORDER BY c.createdAt DESC")
    List<Complaint> findWithFilters(@Param("status") Complaint.Status status,
                                    @Param("category") Complaint.Category category,
                                    @Param("priority") Complaint.Priority priority,
                                    @Param("departmentId") Long departmentId);

    // Find all complaints with multiple filters (paged)
    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:departmentId IS NULL OR c.department.id = :departmentId)")
    Page<Complaint> findWithFiltersPaged(@Param("status") Complaint.Status status,
                                         @Param("category") Complaint.Category category,
                                         @Param("priority") Complaint.Priority priority,
                                         @Param("departmentId") Long departmentId,
                                         Pageable pageable);

    // SLA engine: find non-resolved, non-rejected complaints past SLA deadline that are not yet marked overdue
    @Query("SELECT c FROM Complaint c WHERE c.status NOT IN ('RESOLVED', 'REJECTED') " +
           "AND c.slaDeadline IS NOT NULL AND c.slaDeadline < :now AND c.isOverdue = false")
    List<Complaint> findPendingBreachedSlaComplaints(@Param("now") LocalDateTime now);
}
