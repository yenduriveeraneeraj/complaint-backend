package com.example.complaintmanagement.repository;

import com.example.complaintmanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByIsActiveOrderByNameAsc(Boolean isActive);
    boolean existsByName(String name);
}
