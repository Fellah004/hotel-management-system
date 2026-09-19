package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.AssignMaintenanceRequest;
import com.hms.operationsservice.dto.request.CreateMaintenanceRequest;
import com.hms.operationsservice.dto.request.ResolveMaintenanceRequest;
import com.hms.operationsservice.dto.request.UpdateMaintenanceStatusRequest;
import com.hms.operationsservice.dto.response.MaintenanceResponse;
import com.hms.operationsservice.security.UserPrincipal;
import com.hms.operationsservice.service.OperationsService;
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
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@Tag(name = "Room Maintenance Operations", description = "APIs for reporting, assigning, and resolving room maintenance issues")
@SecurityRequirement(name = "BearerAuth")
public class MaintenanceController {

    private final OperationsService operationsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Report maintenance issue", description = "Manager/Owner reports an issue and puts room under MAINTENANCE")
    public ResponseEntity<MaintenanceResponse> createMaintenanceIssue(@Valid @RequestBody CreateMaintenanceRequest request) {
        return new ResponseEntity<>(operationsService.createMaintenanceIssue(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'STAFF', 'HOUSEKEEPER', 'MAINTENANCE_STAFF')")
    @Operation(summary = "List maintenance issues", description = "Retrieves all maintenance issues")
    public ResponseEntity<List<MaintenanceResponse>> getAllMaintenanceIssues() {
        return ResponseEntity.ok(operationsService.getAllMaintenanceIssues());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'STAFF', 'HOUSEKEEPER', 'MAINTENANCE_STAFF')")
    @Operation(summary = "Get maintenance issue by ID", description = "Retrieves maintenance issue details")
    public ResponseEntity<MaintenanceResponse> getMaintenanceIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(operationsService.getMaintenanceIssueById(id));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Assign maintenance issue", description = "Manager/Owner assigns maintenance task to staff")
    public ResponseEntity<MaintenanceResponse> assignMaintenance(@PathVariable Long id,
                                                                 @Valid @RequestBody AssignMaintenanceRequest request) {
        return ResponseEntity.ok(operationsService.assignMaintenance(id, request));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('STAFF', 'HOUSEKEEPER', 'MAINTENANCE_STAFF', 'EMPLOYEE')")
    @Operation(summary = "Accept maintenance task", description = "Assigned staff exclusively accepts the maintenance task")
    public ResponseEntity<MaintenanceResponse> acceptMaintenance(@PathVariable Long id,
                                                                 @RequestParam(value = "staffId", required = false) Long staffId,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        Long effectiveStaffId = (staffId != null) ? staffId : (principal != null ? principal.getId() : null);
        return ResponseEntity.ok(operationsService.acceptMaintenance(id, effectiveStaffId));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('STAFF', 'HOUSEKEEPER', 'MAINTENANCE_STAFF', 'EMPLOYEE')")
    @Operation(summary = "Start maintenance task", description = "Assigned staff begins repair work; marks issue IN_PROGRESS")
    public ResponseEntity<MaintenanceResponse> startMaintenance(@PathVariable Long id,
                                                                @RequestParam(value = "staffId", required = false) Long staffId,
                                                                @AuthenticationPrincipal UserPrincipal principal) {
        Long effectiveStaffId = (staffId != null) ? staffId : (principal != null ? principal.getId() : null);
        return ResponseEntity.ok(operationsService.startMaintenance(id, effectiveStaffId));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('STAFF', 'HOUSEKEEPER', 'MAINTENANCE_STAFF', 'EMPLOYEE')")
    @Operation(summary = "Reject maintenance task", description = "Assigned staff rejects task with remarks/reason")
    public ResponseEntity<MaintenanceResponse> rejectMaintenance(@PathVariable Long id,
                                                                 @RequestParam(value = "staffId", required = false) Long staffId,
                                                                 @RequestParam(value = "remarks", defaultValue = "Rejected by staff") String remarks,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        Long effectiveStaffId = (staffId != null) ? staffId : (principal != null ? principal.getId() : null);
        return ResponseEntity.ok(operationsService.rejectMaintenance(id, effectiveStaffId, remarks));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('STAFF', 'HOUSEKEEPER', 'MAINTENANCE_STAFF', 'EMPLOYEE')")
    @Operation(summary = "Resolve maintenance task", description = "Strictly assigned staff only: resolves maintenance task, recording cost and remarks")
    public ResponseEntity<MaintenanceResponse> resolveMaintenance(@PathVariable Long id,
                                                                  @RequestParam(value = "staffId", required = false) Long staffId,
                                                                  @RequestBody(required = false) ResolveMaintenanceRequest request,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        Long effectiveStaffId = (staffId != null) ? staffId : (principal != null ? principal.getId() : null);
        return ResponseEntity.ok(operationsService.resolveMaintenance(id, effectiveStaffId, request));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Verify maintenance task", description = "Manager/Owner verifies the completed repair work and updates room status to AVAILABLE")
    public ResponseEntity<MaintenanceResponse> verifyMaintenance(@PathVariable Long id,
                                                                 @RequestParam(value = "managerId", required = false) Long managerId,
                                                                 @AuthenticationPrincipal UserPrincipal principal) {
        Long effectiveManagerId = (managerId != null) ? managerId : (principal != null ? principal.getId() : 1L);
        return ResponseEntity.ok(operationsService.verifyMaintenance(id, effectiveManagerId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Update maintenance status", description = "Updates status; when AVAILABLE or VERIFIED, releases room to AVAILABLE state")
    public ResponseEntity<MaintenanceResponse> updateMaintenanceStatus(@PathVariable Long id,
                                                                       @Valid @RequestBody UpdateMaintenanceStatusRequest request) {
        return ResponseEntity.ok(operationsService.updateMaintenanceStatus(id, request));
    }
}
