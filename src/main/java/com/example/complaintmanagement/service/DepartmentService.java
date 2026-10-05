package com.example.complaintmanagement.service;

import com.example.complaintmanagement.entity.*;
import com.example.complaintmanagement.exception.*;
import com.example.complaintmanagement.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<Department> getAllActive() {
        return departmentRepository.findByIsActiveOrderByNameAsc(true);
    }

    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    public Department create(String name, String description) {
        if (departmentRepository.existsByName(name)) {
            throw new BadRequestException("Department '" + name + "' already exists.");
        }
        Department dept = Department.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .build();
        return departmentRepository.save(dept);
    }

    public Department update(Long id, String name, String description) {
        Department dept = getById(id);
        dept.setName(name);
        dept.setDescription(description);
        return departmentRepository.save(dept);
    }

    public Department toggleStatus(Long id) {
        Department dept = getById(id);
        dept.setIsActive(!dept.getIsActive());
        return departmentRepository.save(dept);
    }
}
