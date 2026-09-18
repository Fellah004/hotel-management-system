package com.hms.purchaseservice.controller;

import com.hms.purchaseservice.dto.request.ApprovePurchaseRequest;
import com.hms.purchaseservice.dto.request.CreatePurchaseRequest;
import com.hms.purchaseservice.dto.request.RejectPurchaseRequest;
import com.hms.purchaseservice.dto.response.PurchaseRequestResponse;
import com.hms.purchaseservice.entity.RequestStatus;
import com.hms.purchaseservice.security.UserPrincipal;
import com.hms.purchaseservice.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/purchase-requests")
@RequiredArgsConstructor
@Tag(name = "Purchase Request Management", description = "Endpoints for hotel staff purchase requests and manager approvals")
public class PurchaseRequestController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Create a new purchase request")
    public ResponseEntity<PurchaseRequestResponse> createPurchaseRequest(
            @Valid @RequestBody CreatePurchaseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long requesterId = principal != null ? principal.getId() : 1L;
        String role = principal != null ? principal.getRole() : "STAFF";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseService.createPurchaseRequest(request, requesterId, role));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Get purchase request by ID")
    public ResponseEntity<PurchaseRequestResponse> getPurchaseRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseRequestById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Get all purchase requests with optional status filter")
    public ResponseEntity<List<PurchaseRequestResponse>> getAllPurchaseRequests(
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(purchaseService.getAllPurchaseRequests(status));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Get purchase requests initiated by the authenticated user")
    public ResponseEntity<List<PurchaseRequestResponse>> getMyPurchaseRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long requesterId = principal != null ? principal.getId() : 1L;
        return ResponseEntity.ok(purchaseService.getPurchaseRequestsByRequester(requesterId));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Approve a submitted purchase request")
    public ResponseEntity<PurchaseRequestResponse> approvePurchaseRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovePurchaseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && request.getManagerId() == null) {
            request.setManagerId(principal.getId());
        }
        return ResponseEntity.ok(purchaseService.approvePurchaseRequest(id, request));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Reject a submitted purchase request")
    public ResponseEntity<PurchaseRequestResponse> rejectPurchaseRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectPurchaseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && request.getManagerId() == null) {
            request.setManagerId(principal.getId());
        }
        return ResponseEntity.ok(purchaseService.rejectPurchaseRequest(id, request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Cancel a purchase request")
    public ResponseEntity<PurchaseRequestResponse> cancelPurchaseRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long requesterId = principal != null ? principal.getId() : 1L;
        String role = principal != null ? principal.getRole() : "STAFF";
        return ResponseEntity.ok(purchaseService.cancelPurchaseRequest(id, requesterId, role));
    }
}
