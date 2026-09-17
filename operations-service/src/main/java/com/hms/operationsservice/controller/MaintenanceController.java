package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.AssignMaintenanceRequest;
import com.hms.operationsservice.dto.request.CreateMaintenanceRequest;
import com.hms.operationsservice.dto.request.UpdateMaintenanceStatusRequest;
import com.hms.operationsservice.dto.response.MaintenanceResponse;
import com.hms.operationsservice.service.OperationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'RECEPTIONIST')")
    @Operation(summary = "Report maintenance issue", description = "Reports an issue; marks room MAINTENANCE")
    public ResponseEntity<MaintenanceResponse> createMaintenanceIssue(@Valid @RequestBody CreateMaintenanceRequest request) {
        return new ResponseEntity<>(operationsService.createMaintenanceIssue(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "List maintenance issues", description = "Retrieves all maintenance issues")
    public ResponseEntity<List<MaintenanceResponse>> getAllMaintenanceIssues() {
        return ResponseEntity.ok(operationsService.getAllMaintenanceIssues());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Get maintenance issue by ID", description = "Retrieves maintenance issue details")
    public ResponseEntity<MaintenanceResponse> getMaintenanceIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(operationsService.getMaintenanceIssueById(id));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Assign maintenance issue", description = "Assigns maintenance task to staff")
    public ResponseEntity<MaintenanceResponse> assignMaintenance(@PathVariable Long id,
                                                                 @Valid @RequestBody AssignMaintenanceRequest request) {
        return ResponseEntity.ok(operationsService.assignMaintenance(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Update maintenance status", description = "Updates status; when AVAILABLE, releases room to AVAILABLE state")
    public ResponseEntity<MaintenanceResponse> updateMaintenanceStatus(@PathVariable Long id,
                                                                       @Valid @RequestBody UpdateMaintenanceStatusRequest request) {
        return ResponseEntity.ok(operationsService.updateMaintenanceStatus(id, request));
    }
}
