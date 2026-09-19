package com.hms.staffservice.controller;

import com.hms.staffservice.dto.request.CreateStaffRequest;
import com.hms.staffservice.dto.request.UpdateStaffRequest;
import com.hms.staffservice.dto.response.StaffResponse;
import com.hms.staffservice.entity.StaffRole;
import com.hms.staffservice.security.UserPrincipal;
import com.hms.staffservice.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "Endpoints for managing staff members, roles, salaries, and profiles")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Create staff profile", description = "Admin/Owner/Manager registers a new staff member")
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        StaffResponse response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Get all staff", description = "Retrieves staff list with role-based salary/NIC data masking")
    public ResponseEntity<List<StaffResponse>> getAllStaff(
            @RequestParam(required = false) StaffRole role,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userRole = principal != null ? principal.getRole() : "ANONYMOUS";
        if (role != null) {
            return ResponseEntity.ok(staffService.getStaffByRole(role, userRole));
        }
        return ResponseEntity.ok(staffService.getAllStaff(userRole));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff by ID", description = "Retrieves staff details by ID")
    public ResponseEntity<StaffResponse> getStaffById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userRole = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(staffService.getStaffById(id, userRole));
    }

    @GetMapping("/code/{employeeCode}")
    @Operation(summary = "Get staff by Employee Code", description = "Retrieves staff by employee code")
    public ResponseEntity<StaffResponse> getStaffByEmployeeCode(
            @PathVariable String employeeCode,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userRole = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(staffService.getStaffByEmployeeCode(employeeCode, userRole));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get staff by User ID", description = "Retrieves staff profile linked to user account")
    public ResponseEntity<StaffResponse> getStaffByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userRole = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(staffService.getStaffByUserId(userId, userRole));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'HOUSEKEEPER', 'STAFF', 'MAINTENANCE_STAFF')")
    @Operation(summary = "Update staff profile", description = "Updates staff details (Admin/Owner can update any; Staff can only update their own profile without changing role/salary)")
    public ResponseEntity<StaffResponse> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userRole = principal != null ? principal.getRole() : "ANONYMOUS";
        Long currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(staffService.updateStaff(id, request, currentUserId, userRole));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Delete staff member", description = "Deletes staff record (Admin/Owner only)")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }
}
