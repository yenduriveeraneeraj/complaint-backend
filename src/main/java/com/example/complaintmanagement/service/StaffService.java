package com.example.complaintmanagement.service;

import com.example.complaintmanagement.entity.*;
import com.example.complaintmanagement.exception.*;
import com.example.complaintmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final DepartmentRepository departmentRepository;

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public List<Staff> getStaffByDepartment(Long departmentId) {
        return staffRepository.findByDepartmentIdAndIsActive(departmentId, true);
    }

    public Staff getById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
    }

    public Staff create(String name, String email, String phone,
                        String designation, Long departmentId) {
        if (staffRepository.existsByEmail(email)) {
            throw new BadRequestException("Staff with email '" + email + "' already exists.");
        }
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));

        Staff staff = Staff.builder()
                .name(name).email(email).phone(phone)
                .designation(designation).department(dept).isActive(true)
                .build();
        return staffRepository.save(staff);
    }

    public Staff update(Long id, String name, String phone,
                        String designation, Long departmentId) {
        Staff staff = getById(id);
        staff.setName(name);
        staff.setPhone(phone);
        staff.setDesignation(designation);

        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));
        staff.setDepartment(dept);
        return staffRepository.save(staff);
    }

    public Staff toggleStatus(Long id) {
        Staff staff = getById(id);
        staff.setIsActive(!staff.getIsActive());
        return staffRepository.save(staff);
    }
}
