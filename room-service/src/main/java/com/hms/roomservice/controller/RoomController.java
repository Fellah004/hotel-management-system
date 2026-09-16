package com.hms.roomservice.controller;

import com.hms.roomservice.dto.request.CreateRoomRequest;
import com.hms.roomservice.dto.request.RoomStatusUpdateRequest;
import com.hms.roomservice.dto.request.UpdateRoomRequest;
import com.hms.roomservice.dto.response.RoomResponse;
import com.hms.roomservice.entity.ResourceType;
import com.hms.roomservice.entity.RoomStatus;
import com.hms.roomservice.security.UserPrincipal;
import com.hms.roomservice.service.RoomService;
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
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms & Halls", description = "Endpoints for managing rooms, bookable halls, status transitions, and availability")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create room or hall", description = "Admin/Owner/Manager creates a new room or hall resource")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all rooms", description = "Retrieves list of all rooms and halls")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID", description = "Retrieves room details by ID")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/number/{roomNumber}")
    @Operation(summary = "Get room by room number", description = "Retrieves room by room number")
    public ResponseEntity<RoomResponse> getRoomByNumber(@PathVariable String roomNumber) {
        return ResponseEntity.ok(roomService.getRoomByNumber(roomNumber));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available rooms", description = "Finds available rooms by category, minimum capacity, and resource type")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) ResourceType resourceType) {
        return ResponseEntity.ok(roomService.getAvailableRooms(categoryId, minCapacity, resourceType));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get rooms by status", description = "Retrieves rooms by status (DIRTY, CLEAN, MAINTENANCE, etc.)")
    public ResponseEntity<List<RoomResponse>> getRoomsByStatus(@PathVariable RoomStatus status) {
        return ResponseEntity.ok(roomService.getRoomsByStatus(status));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update room", description = "Updates room attributes")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id, @Valid @RequestBody UpdateRoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'HOUSEKEEPER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update room status", description = "Updates room status following the state machine rules")
    public ResponseEntity<RoomResponse> updateRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String role = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(roomService.updateRoomStatus(id, request, role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete room", description = "Deletes a room by ID")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
