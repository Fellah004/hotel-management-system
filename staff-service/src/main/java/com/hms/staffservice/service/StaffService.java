package com.hms.staffservice.service;

import com.hms.staffservice.dto.request.CreateStaffRequest;
import com.hms.staffservice.dto.request.UpdateStaffRequest;
import com.hms.staffservice.dto.response.StaffResponse;
import com.hms.staffservice.entity.StaffRole;

import java.util.List;

public interface StaffService {
    StaffResponse createStaff(CreateStaffRequest request);
    StaffResponse getStaffById(Long id, String currentUserRole);
    StaffResponse getStaffByEmployeeCode(String employeeCode, String currentUserRole);
    StaffResponse getStaffByUserId(Long userId, String currentUserRole);
    List<StaffResponse> getAllStaff(String currentUserRole);
    List<StaffResponse> getStaffByRole(StaffRole role, String currentUserRole);
    StaffResponse updateStaff(Long id, UpdateStaffRequest request, String currentUserRole);
    void deleteStaff(Long id);
}
