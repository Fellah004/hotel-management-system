package com.hms.staffservice.controller;

import com.hms.staffservice.dto.request.LeaveApprovalRequest;
import com.hms.staffservice.dto.request.LeaveRequest;
import com.hms.staffservice.dto.response.LeaveResponse;
import com.hms.staffservice.security.UserPrincipal;
import com.hms.staffservice.service.LeaveService;
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
@RequestMapping("/api/leave")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Endpoints for staff leave requests and admin/owner approvals")
@SecurityRequirement(name = "bearerAuth")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'HOUSEKEEPER', 'MANAGER')")
    @Operation(summary = "Submit leave request", description = "Receptionist, Housekeeper, or Manager submits a leave application for themselves")
    public ResponseEntity<LeaveResponse> requestLeave(
            @Valid @RequestBody LeaveRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        LeaveResponse response = leaveService.requestLeave(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Approve or reject leave", description = "Admin or Owner approves or rejects a pending leave request")
    public ResponseEntity<LeaveResponse> approveOrRejectLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long approverUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(leaveService.approveOrRejectLeave(id, request, approverUserId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID", description = "Retrieves leave request details")
    public ResponseEntity<LeaveResponse> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeaveById(id));
    }

    @GetMapping("/staff/{staffId}")
    @Operation(summary = "Get leave history by staff", description = "Retrieves leave applications for a staff member")
    public ResponseEntity<List<LeaveResponse>> getLeavesByStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(leaveService.getLeavesByStaff(staffId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Get all leave requests", description = "Retrieves all leave applications across the hotel")
    public ResponseEntity<List<LeaveResponse>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }
}
