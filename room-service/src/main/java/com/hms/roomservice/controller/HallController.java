package com.hms.roomservice.controller;

import com.hms.roomservice.dto.request.CreateHallRequest;
import com.hms.roomservice.dto.request.HallStatusUpdateRequest;
import com.hms.roomservice.dto.request.UpdateHallRequest;
import com.hms.roomservice.dto.response.HallResponse;
import com.hms.roomservice.entity.HallStatus;
import com.hms.roomservice.service.HallService;
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
@RequestMapping("/api/halls")
@RequiredArgsConstructor
@Tag(name = "Halls", description = "Endpoints for managing event & banquet halls, layout seating capacities, availability, and statuses")
public class HallController {

    private final HallService hallService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create hall", description = "Admin/Owner/Manager creates a new event or banquet hall with seating layout capacities")
    public ResponseEntity<HallResponse> createHall(@Valid @RequestBody CreateHallRequest request) {
        HallResponse response = hallService.createHall(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all halls", description = "Retrieves list of active event and banquet halls (or all halls if includeInactive=true)")
    public ResponseEntity<List<HallResponse>> getAllHalls(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(hallService.getAllHalls(includeInactive));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hall by ID", description = "Retrieves hall details by ID")
    public ResponseEntity<HallResponse> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.getHallById(id));
    }

    @GetMapping("/number/{hallNumber}")
    @Operation(summary = "Get hall by hall number / code", description = "Retrieves hall by hall number (e.g. HALL-A)")
    public ResponseEntity<HallResponse> getHallByNumber(@PathVariable String hallNumber) {
        return ResponseEntity.ok(hallService.getHallByNumber(hallNumber));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available halls", description = "Finds available halls filtered by category and minimum capacity")
    public ResponseEntity<List<HallResponse>> getAvailableHalls(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minCapacity) {
        return ResponseEntity.ok(hallService.getAvailableHalls(categoryId, minCapacity));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get halls by status", description = "Retrieves active halls by status (AVAILABLE, BOOKED, SETUP_IN_PROGRESS, EVENT_ONGOING, CLEANUP, MAINTENANCE)")
    public ResponseEntity<List<HallResponse>> getHallsByStatus(
            @PathVariable HallStatus status,
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(hallService.getHallsByStatus(status, includeInactive));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update hall", description = "Updates hall details, pricing, layout capacities, and amenities")
    public ResponseEntity<HallResponse> updateHall(@PathVariable Long id, @Valid @RequestBody UpdateHallRequest request) {
        return ResponseEntity.ok(hallService.updateHall(id, request));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Activate hall", description = "Activates a deactivated hall so it appears in listings and becomes bookable")
    public ResponseEntity<HallResponse> activateHall(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.activateHall(id));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deactivate hall", description = "Deactivates a hall so it is hidden from listings and cannot be booked")
    public ResponseEntity<HallResponse> deactivateHall(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.deactivateHall(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'HOUSEKEEPER', 'STAFF')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update hall status", description = "Updates hall lifecycle status (AVAILABLE, BOOKED, SETUP_IN_PROGRESS, etc.)")
    public ResponseEntity<HallResponse> updateHallStatus(
            @PathVariable Long id,
            @Valid @RequestBody HallStatusUpdateRequest request) {
        return ResponseEntity.ok(hallService.updateHallStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete hall", description = "Deletes a hall by ID (Admin/Owner only)")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }
}
