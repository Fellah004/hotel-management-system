package com.hms.staffservice.controller;

import com.hms.staffservice.dto.request.ShiftRequest;
import com.hms.staffservice.dto.response.ShiftResponse;
import com.hms.staffservice.service.ShiftService;
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
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@Tag(name = "Shift Management", description = "Endpoints for scheduling shifts and assigning staff")
@SecurityRequirement(name = "bearerAuth")
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Create shift", description = "Creates a morning, evening, or night shift schedule")
    public ResponseEntity<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.createShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all shifts", description = "Retrieves all defined hotel work shifts")
    public ResponseEntity<List<ShiftResponse>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shift by ID", description = "Retrieves shift details and assigned staff list")
    public ResponseEntity<ShiftResponse> getShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @GetMapping("/staff/{staffId}")
    @Operation(summary = "Get shifts for staff", description = "Retrieves shifts assigned to a specific staff member")
    public ResponseEntity<List<ShiftResponse>> getShiftsByStaffId(@PathVariable Long staffId) {
        return ResponseEntity.ok(shiftService.getShiftsByStaffId(staffId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Update shift", description = "Updates shift timings or active status")
    public ResponseEntity<ShiftResponse> updateShift(@PathVariable Long id, @Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.ok(shiftService.updateShift(id, request));
    }

    @PostMapping("/{shiftId}/assign/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Assign staff to shift", description = "Assigns a staff member to a designated work shift")
    public ResponseEntity<ShiftResponse> assignStaffToShift(@PathVariable Long shiftId, @PathVariable Long staffId) {
        return ResponseEntity.ok(shiftService.assignStaffToShift(shiftId, staffId));
    }

    @DeleteMapping("/{shiftId}/assign/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Remove staff from shift", description = "Unassigns staff member from shift")
    public ResponseEntity<ShiftResponse> removeStaffFromShift(@PathVariable Long shiftId, @PathVariable Long staffId) {
        return ResponseEntity.ok(shiftService.removeStaffFromShift(shiftId, staffId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Delete shift", description = "Deletes a shift definition (Admin/Owner only)")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}
