package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ComplaintRepository — all database queries for Complaint.
 *
 * Key queries:
 *   - Find complaints by user (for user's complaint list)
 *   - Find by status (for filtering)
 *   - Find by complaint number (for lookup)
 *   - Count by status (for dashboard stats)
 *   - Date-range filtering (for reports)
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Find all complaints by a specific user — used in "My Complaints"
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find complaint by its unique complaint number (e.g., CMP-2026-00001)
    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    // Count complaints by status — for dashboard stats
    long countByStatus(Complaint.Status status);

    // Count complaints by user ID and status — for user dashboard
    long countByUserIdAndStatus(Long userId, Complaint.Status status);

    // Count complaints for a specific user
    long countByUserId(Long userId);

    // Find complaints filtered by status (admin)
    List<Complaint> findByStatusOrderByCreatedAtDesc(Complaint.Status status);

    // Find complaints by department
    List<Complaint> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    // Find complaints by category
    List<Complaint> findByCategoryOrderByCreatedAtDesc(Complaint.Category category);

    // Find the latest complaint number to generate the next one
    @Query("SELECT c.complaintNumber FROM Complaint c ORDER BY c.id DESC")
    List<String> findLatestComplaintNumbers();

    // Admin: search complaints by keyword in title
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

    // Find all complaints with multiple filters (admin complaint management)
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
}
