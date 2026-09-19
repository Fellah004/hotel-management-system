package com.hms.purchaseservice.controller;

import com.hms.purchaseservice.dto.request.CreateQuotationRequest;
import com.hms.purchaseservice.dto.response.SupplierQuotationResponse;
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
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
@Tag(name = "Supplier Quotations", description = "Endpoints for managing supplier bids, RFQs, and quotation selection")
public class QuotationController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Record a supplier quotation for a purchase request")
    public ResponseEntity<SupplierQuotationResponse> createQuotation(
            @Valid @RequestBody CreateQuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createQuotation(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Get quotation by ID")
    public ResponseEntity<SupplierQuotationResponse> getQuotationById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getQuotationById(id));
    }

    @GetMapping("/purchase-request/{prId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Get all quotations submitted for a purchase request")
    public ResponseEntity<List<SupplierQuotationResponse>> getQuotationsByPurchaseRequest(@PathVariable Long prId) {
        return ResponseEntity.ok(purchaseService.getQuotationsByPurchaseRequest(prId));
    }

    @PutMapping("/{id}/select")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Select and accept a winning quotation")
    public ResponseEntity<SupplierQuotationResponse> selectQuotation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long managerId = principal != null ? principal.getId() : 1L;
        return ResponseEntity.ok(purchaseService.selectQuotation(id, managerId));
    }
}
