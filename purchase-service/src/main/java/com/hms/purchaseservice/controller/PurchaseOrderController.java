package com.hms.purchaseservice.controller;

import com.hms.purchaseservice.dto.request.ApprovePurchaseOrderRequest;
import com.hms.purchaseservice.dto.request.CreatePurchaseOrderRequest;
import com.hms.purchaseservice.dto.response.PurchaseOrderResponse;
import com.hms.purchaseservice.entity.OrderStatus;
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
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Order Management", description = "Endpoints for creating, approving, and dispatching Purchase Orders")
public class PurchaseOrderController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Generate a new purchase order")
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long staffId = principal != null ? principal.getId() : 1L;
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createPurchaseOrder(request, staffId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseOrderById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Get all purchase orders with optional status filter")
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders(
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(purchaseService.getAllPurchaseOrders(status));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Approve a draft purchase order")
    public ResponseEntity<PurchaseOrderResponse> approvePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody ApprovePurchaseOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && request.getManagerId() == null) {
            request.setManagerId(principal.getId());
        }
        return ResponseEntity.ok(purchaseService.approvePurchaseOrder(id, request));
    }

    @PutMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Dispatch purchase order to supplier")
    public ResponseEntity<PurchaseOrderResponse> sendPurchaseOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long staffId = principal != null ? principal.getId() : 1L;
        return ResponseEntity.ok(purchaseService.sendPurchaseOrderToSupplier(id, staffId));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Cancel a purchase order")
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long managerId = principal != null ? principal.getId() : 1L;
        String remarks = (body != null && body.containsKey("remarks")) ? body.get("remarks") : "Cancelled by manager";
        return ResponseEntity.ok(purchaseService.cancelPurchaseOrder(id, managerId, remarks));
    }
}
