package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.ComplaintStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {
    // Find all history entries for a complaint, sorted oldest first (for timeline display)
    List<ComplaintStatusHistory> findByComplaintIdOrderByChangedAtAsc(Long complaintId);
}
