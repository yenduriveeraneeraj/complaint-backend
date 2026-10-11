package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByDepartmentId(Long departmentId);
    List<Staff> findByDepartmentIdAndIsActive(Long departmentId, Boolean isActive);
    List<Staff> findByIsActive(Boolean isActive);
    boolean existsByEmail(String email);
    Optional<Staff> findByUserId(Long userId);
    Optional<Staff> findByEmail(String email);
}
