package com.hms.purchaseservice.controller;

import com.hms.purchaseservice.dto.request.CreateGoodsReceiptRequest;
import com.hms.purchaseservice.dto.response.GoodsReceiptResponse;
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
@RequestMapping("/api/goods-receipts")
@RequiredArgsConstructor
@Tag(name = "Goods Receiving (GRN)", description = "Endpoints for logging received deliveries, inspections, and partial/full receipts")
public class GoodsReceiptController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE')")
    @Operation(summary = "Record incoming goods receipt (GRN)")
    public ResponseEntity<GoodsReceiptResponse> recordGoodsReceipt(
            @Valid @RequestBody CreateGoodsReceiptRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && request.getReceivedByStaffId() == null) {
            request.setReceivedByStaffId(principal.getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.recordGoodsReceipt(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Get goods receipt by ID")
    public ResponseEntity<GoodsReceiptResponse> getGoodsReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getGoodsReceiptById(id));
    }

    @GetMapping("/purchase-order/{poId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER', 'MAINTENANCE', 'RECEPTIONIST')")
    @Operation(summary = "Get all goods receipts recorded for a purchase order")
    public ResponseEntity<List<GoodsReceiptResponse>> getGoodsReceiptsByPurchaseOrder(@PathVariable Long poId) {
        return ResponseEntity.ok(purchaseService.getGoodsReceiptsByPurchaseOrder(poId));
    }
}
