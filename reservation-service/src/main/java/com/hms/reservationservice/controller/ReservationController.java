package com.hms.reservationservice.controller;

import com.hms.reservationservice.dto.request.*;
import com.hms.reservationservice.dto.response.ReservationResponse;
import com.hms.reservationservice.security.UserPrincipal;
import com.hms.reservationservice.service.ReservationService;
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
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Endpoints for hotel room & hall bookings, lifecycle state machine, transfers, upgrades, and check-in/out")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create reservation", description = "Creates reservation in PENDING state, calculates dynamic pricing, and reserves room hold")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Get all reservations", description = "Staff access to list all reservations")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieves reservation by ID with object-level security check")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(reservationService.getReservationById(id, userId, role));
    }

    @GetMapping("/code/{reservationCode}")
    @Operation(summary = "Get reservation by Code", description = "Retrieves reservation by unique code")
    public ResponseEntity<ReservationResponse> getReservationByCode(
            @PathVariable String reservationCode,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(reservationService.getReservationByCode(reservationCode, userId, role));
    }

    @GetMapping("/guest/{guestId}")
    @Operation(summary = "Get reservations by guest ID", description = "Retrieves reservations for a specific guest")
    public ResponseEntity<List<ReservationResponse>> getReservationsByGuestId(
            @PathVariable Long guestId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(reservationService.getReservationsByGuestId(guestId, userId, role));
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Get reservations by room ID", description = "Retrieves reservations for a specific room")
    public ResponseEntity<List<ReservationResponse>> getReservationsByRoomId(@PathVariable Long roomId) {
        return ResponseEntity.ok(reservationService.getReservationsByRoomId(roomId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reservation", description = "Updates reservation details")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(reservationService.updateReservation(id, request, userId, role));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm reservation", description = "Transitions reservation from PENDING to CONFIRMED state and updates resource status to BOOKED")
    public ResponseEntity<ReservationResponse> confirmReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(reservationService.confirmReservation(id, userId, role));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel reservation", description = "Cancels a PENDING or CONFIRMED reservation and releases room")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(reservationService.cancelReservation(id, reason, userId, role));
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Check In Guest", description = "Transitions reservation to CHECKED_IN and room to OCCUPIED")
    public ResponseEntity<ReservationResponse> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.checkIn(id));
    }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Check Out Guest", description = "Transitions reservation to CHECKED_OUT and room to DIRTY for Housekeeping")
    public ResponseEntity<ReservationResponse> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.checkOut(id));
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Mark No-Show", description = "Marks confirmed reservation as NO_SHOW and releases room")
    public ResponseEntity<ReservationResponse> markNoShow(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.markNoShow(id));
    }

    @PostMapping("/{id}/room-transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Transfer Room", description = "Transfers guest to another available room")
    public ResponseEntity<ReservationResponse> transferRoom(@PathVariable Long id, @Valid @RequestBody RoomTransferRequest request) {
        return ResponseEntity.ok(reservationService.transferRoom(id, request));
    }

    @PostMapping("/{id}/room-upgrade")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Upgrade Room", description = "Upgrades reservation to higher category with dynamic pricing quote update")
    public ResponseEntity<ReservationResponse> upgradeRoom(@PathVariable Long id, @Valid @RequestBody RoomUpgradeRequest request) {
        return ResponseEntity.ok(reservationService.upgradeRoom(id, request));
    }

    @PostMapping("/{id}/room-downgrade")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Downgrade Room", description = "Downgrades reservation to lower category with price adjustment")
    public ResponseEntity<ReservationResponse> downgradeRoom(@PathVariable Long id, @Valid @RequestBody RoomDowngradeRequest request) {
        return ResponseEntity.ok(reservationService.downgradeRoom(id, request));
    }

    @PostMapping("/{id}/early-check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Early Check-in", description = "Handles early check-in arrival request")
    public ResponseEntity<ReservationResponse> earlyCheckIn(@PathVariable Long id, @Valid @RequestBody EarlyCheckInRequest request) {
        return ResponseEntity.ok(reservationService.earlyCheckIn(id, request));
    }

    @PostMapping("/{id}/late-checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Late Checkout", description = "Handles late checkout extension request")
    public ResponseEntity<ReservationResponse> lateCheckout(@PathVariable Long id, @Valid @RequestBody LateCheckoutRequest request) {
        return ResponseEntity.ok(reservationService.lateCheckout(id, request));
    }

    @GetMapping("/{id}/price-breakdown")
    @Operation(summary = "Get stay price breakdown", description = "Retrieves itemized daily pricing breakdown including base rates and dynamic multipliers for this reservation")
    public ResponseEntity<com.hms.reservationservice.client.dto.RateQuoteResponseDto> getPriceBreakdown(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : "ANONYMOUS";
        return ResponseEntity.ok(reservationService.getPriceBreakdown(id, userId, role));
    }

    @PostMapping("/preview-quote")
    @Operation(summary = "Preview dynamic price quote", description = "Calculates stay pricing breakdown for selected category and dates before booking")
    public ResponseEntity<com.hms.reservationservice.client.dto.RateQuoteResponseDto> previewQuote(
            @Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.ok(reservationService.previewQuote(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Delete reservation", description = "Deletes reservation record (Admin/Owner only)")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
