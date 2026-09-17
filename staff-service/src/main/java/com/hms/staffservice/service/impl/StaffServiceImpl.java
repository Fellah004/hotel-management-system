package com.hms.staffservice.service.impl;

import com.hms.staffservice.dto.request.CreateStaffRequest;
import com.hms.staffservice.dto.request.UpdateStaffRequest;
import com.hms.staffservice.dto.response.StaffResponse;
import com.hms.staffservice.entity.Staff;
import com.hms.staffservice.entity.StaffRole;
import com.hms.staffservice.exception.DuplicateResourceException;
import com.hms.staffservice.exception.ResourceNotFoundException;
import com.hms.staffservice.repository.StaffRepository;
import com.hms.staffservice.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;

    @Override
    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request) {
        if (staffRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException("Staff with employee code already exists: " + request.getEmployeeCode());
        }
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Staff with email already exists: " + request.getEmail());
        }
        if (staffRepository.existsByNic(request.getNic())) {
            throw new DuplicateResourceException("Staff with NIC already exists: " + request.getNic());
        }

        Staff staff = Staff.builder()
                .userId(request.getUserId())
                .employeeCode(request.getEmployeeCode())
                .fullName(request.getFullName())
                .address(request.getAddress())
                .nic(request.getNic())
                .salary(request.getSalary())
                .age(request.getAge())
                .occupation(request.getOccupation())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .build();

        Staff saved = staffRepository.save(staff);
        log.info("Created staff: {} ({})", saved.getFullName(), saved.getEmployeeCode());
        return mapToResponse(saved, "OWNER");
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long id, String currentUserRole) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        return mapToResponse(staff, currentUserRole);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffByEmployeeCode(String employeeCode, String currentUserRole) {
        Staff staff = staffRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with employee code: " + employeeCode));
        return mapToResponse(staff, currentUserRole);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffByUserId(Long userId, String currentUserRole) {
        Staff staff = staffRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with user id: " + userId));
        return mapToResponse(staff, currentUserRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff(String currentUserRole) {
        return staffRepository.findAll().stream()
                .map(s -> mapToResponse(s, currentUserRole))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByRole(StaffRole role, String currentUserRole) {
        return staffRepository.findByRole(role).stream()
                .map(s -> mapToResponse(s, currentUserRole))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(Long id, UpdateStaffRequest request, String currentUserRole) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(staff.getEmail())) {
            if (staffRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            staff.setEmail(request.getEmail());
        }

        if (request.getNic() != null && !request.getNic().equalsIgnoreCase(staff.getNic())) {
            if (staffRepository.existsByNic(request.getNic())) {
                throw new DuplicateResourceException("NIC already in use: " + request.getNic());
            }
            staff.setNic(request.getNic());
        }

        if (request.getFullName() != null) staff.setFullName(request.getFullName());
        if (request.getAddress() != null) staff.setAddress(request.getAddress());
        if (request.getSalary() != null) staff.setSalary(request.getSalary());
        if (request.getAge() != null) staff.setAge(request.getAge());
        if (request.getOccupation() != null) staff.setOccupation(request.getOccupation());
        if (request.getPhone() != null) staff.setPhone(request.getPhone());
        if (request.getRole() != null) staff.setRole(request.getRole());
        if (request.getActive() != null) staff.setActive(request.getActive());

        Staff updated = staffRepository.save(staff);
        log.info("Updated staff id: {}", id);
        return mapToResponse(updated, currentUserRole);
    }

    @Override
    @Transactional
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Staff not found with id: " + id);
        }
        staffRepository.deleteById(id);
        log.info("Deleted staff id: {}", id);
    }

    private StaffResponse mapToResponse(Staff staff, String currentUserRole) {
        boolean canViewSalary = "ADMIN".equalsIgnoreCase(currentUserRole)
                || "OWNER".equalsIgnoreCase(currentUserRole)
                || "MANAGER".equalsIgnoreCase(currentUserRole);

        return StaffResponse.builder()
                .id(staff.getId())
                .userId(staff.getUserId())
                .employeeCode(staff.getEmployeeCode())
                .fullName(staff.getFullName())
                .address(staff.getAddress())
                .nic(canViewSalary ? staff.getNic() : "********")
                .salary(canViewSalary ? staff.getSalary() : null)
                .age(staff.getAge())
                .occupation(staff.getOccupation())
                .email(staff.getEmail())
                .phone(staff.getPhone())
                .role(staff.getRole())
                .active(staff.isActive())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
