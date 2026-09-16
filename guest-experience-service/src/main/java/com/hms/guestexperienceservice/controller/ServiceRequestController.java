package com.hms.guestexperienceservice.controller;

import com.hms.guestexperienceservice.dto.request.CreateServiceRequest;
import com.hms.guestexperienceservice.dto.response.ServiceRequestHistoryResponse;
import com.hms.guestexperienceservice.dto.response.ServiceRequestResponse;
import com.hms.guestexperienceservice.service.GuestExperienceService;
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
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
@Tag(name = "Service Requests & QR Room Service", description = "APIs for guest service requests (cleaning, towels, food orders, amenities, maintenance)")
@SecurityRequirement(name = "BearerAuth")
public class ServiceRequestController {

    private final GuestExperienceService guestExperienceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Create service request", description = "Submits a new guest service request / QR room request")
    public ResponseEntity<ServiceRequestResponse> createServiceRequest(@Valid @RequestBody CreateServiceRequest request) {
        return new ResponseEntity<>(guestExperienceService.createServiceRequest(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'HOUSEKEEPER', 'GUEST')")
    @Operation(summary = "Get service request by ID", description = "Retrieves service request details")
    public ResponseEntity<ServiceRequestResponse> getServiceRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(guestExperienceService.getServiceRequestById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get requests by reservation ID", description = "Retrieves all requests for a reservation")
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequestsByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(guestExperienceService.getServiceRequestsByReservationId(reservationId));
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get requests by guest ID", description = "Retrieves all service requests submitted by a guest")
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequestsByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestExperienceService.getServiceRequestsByGuestId(guestId));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'GUEST')")
    @Operation(summary = "Cancel service request", description = "Cancels a pending or unfulfilled service request")
    public ResponseEntity<ServiceRequestResponse> cancelServiceRequest(@PathVariable Long id) {
        return ResponseEntity.ok(guestExperienceService.cancelServiceRequest(id));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get service request history", description = "Retrieves the status transition audit history")
    public ResponseEntity<List<ServiceRequestHistoryResponse>> getServiceRequestHistory(@PathVariable Long id) {
        return ResponseEntity.ok(guestExperienceService.getServiceRequestHistory(id));
    }
}
