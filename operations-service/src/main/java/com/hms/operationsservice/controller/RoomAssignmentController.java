package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.CreateRoomAssignmentRequest;
import com.hms.operationsservice.dto.response.RoomAssignmentResponse;
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
@RequestMapping("/api/room-assignments")
@RequiredArgsConstructor
@Tag(name = "Housekeeper Room Assignments", description = "APIs for configuring housekeeper room range assignments (e.g. Rooms 1-50)")
@SecurityRequirement(name = "BearerAuth")
public class RoomAssignmentController {

    private final OperationsService operationsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Create room assignment", description = "Assigns a range of room numbers to a housekeeper")
    public ResponseEntity<RoomAssignmentResponse> createRoomAssignment(@Valid @RequestBody CreateRoomAssignmentRequest request) {
        return new ResponseEntity<>(operationsService.createRoomAssignment(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "List all assignments", description = "Retrieves all active room assignments")
    public ResponseEntity<List<RoomAssignmentResponse>> getAllRoomAssignments() {
        return ResponseEntity.ok(operationsService.getAllRoomAssignments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Get assignment by ID", description = "Retrieves room assignment details")
    public ResponseEntity<RoomAssignmentResponse> getRoomAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(operationsService.getRoomAssignmentById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Delete room assignment", description = "Removes a room assignment")
    public ResponseEntity<Void> deleteRoomAssignment(@PathVariable Long id) {
        operationsService.deleteRoomAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
