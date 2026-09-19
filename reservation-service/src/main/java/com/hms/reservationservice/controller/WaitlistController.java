package com.hms.reservationservice.controller;

import com.hms.reservationservice.client.dto.RoomDto;
import com.hms.reservationservice.dto.request.ConfirmWaitlistRequest;
import com.hms.reservationservice.dto.request.ConvertWaitlistRequest;
import com.hms.reservationservice.dto.request.RejectWaitlistRequest;
import com.hms.reservationservice.dto.request.WaitlistRequest;
import com.hms.reservationservice.dto.response.ReservationResponse;
import com.hms.reservationservice.dto.response.WaitlistResponse;
import com.hms.reservationservice.service.WaitlistService;
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
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
@Tag(name = "Waitlist Management", description = "Endpoints for joining, discovering available rooms, confirming, and converting waitlists into reservations")
@SecurityRequirement(name = "bearerAuth")
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    @Operation(summary = "Join waitlist", description = "Places guest on waitlist queue for desired room category and stay dates")
    public ResponseEntity<WaitlistResponse> joinWaitlist(@Valid @RequestBody WaitlistRequest request) {
        WaitlistResponse response = waitlistService.joinWaitlist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Get all active waitlists", description = "Retrieves current active waitlist queue")
    public ResponseEntity<List<WaitlistResponse>> getAllActiveWaitlists() {
        return ResponseEntity.ok(waitlistService.getAllActiveWaitlists());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get waitlist by ID", description = "Retrieves waitlist details by ID")
    public ResponseEntity<WaitlistResponse> getWaitlistById(@PathVariable Long id) {
        return ResponseEntity.ok(waitlistService.getWaitlistById(id));
    }

    @GetMapping("/guest/{guestId}")
    @Operation(summary = "Get waitlists by guest ID", description = "Retrieves waitlist entries for a specific guest")
    public ResponseEntity<List<WaitlistResponse>> getWaitlistsByGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(waitlistService.getWaitlistsByGuest(guestId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel waitlist", description = "Cancels a waitlist entry")
    public ResponseEntity<Void> cancelWaitlist(@PathVariable Long id) {
        waitlistService.cancelWaitlist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/available-rooms")
    @Operation(summary = "Show rooms matching waitlist requirements", description = "Queries matching category rooms that have no overlapping reservations for the waitlist dates")
    public ResponseEntity<List<RoomDto>> getAvailableRoomsForWaitlist(@PathVariable Long id) {
        return ResponseEntity.ok(waitlistService.getAvailableRoomsForWaitlist(id));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm waitlist offer", description = "Confirms the waitlist when a room becomes available")
    public ResponseEntity<WaitlistResponse> confirmWaitlist(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmWaitlistRequest request) {
        Long roomId = request != null ? request.getRoomId() : null;
        return ResponseEntity.ok(waitlistService.confirmWaitlist(id, roomId));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject waitlist offer", description = "Declines or rejects the offered room for this waitlist")
    public ResponseEntity<WaitlistResponse> rejectWaitlist(
            @PathVariable Long id,
            @RequestBody(required = false) RejectWaitlistRequest request) {
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(waitlistService.rejectWaitlist(id, reason));
    }

    @PutMapping("/{id}/expire")
    @Operation(summary = "Expire waitlist", description = "Marks the waitlist offer as expired")
    public ResponseEntity<WaitlistResponse> expireWaitlist(@PathVariable Long id) {
        return ResponseEntity.ok(waitlistService.expireWaitlist(id));
    }

    @PostMapping("/{id}/convert-to-reservation")
    @Operation(summary = "Convert waitlist to reservation", description = "Converts a confirmed waitlist into an active reservation and links the assigned room")
    public ResponseEntity<ReservationResponse> convertToReservation(
            @PathVariable Long id,
            @RequestBody(required = false) ConvertWaitlistRequest request) {
        Long roomId = request != null ? request.getRoomId() : null;
        String specialRequests = request != null ? request.getSpecialRequests() : null;
        ReservationResponse response = waitlistService.convertToReservation(id, roomId, specialRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
