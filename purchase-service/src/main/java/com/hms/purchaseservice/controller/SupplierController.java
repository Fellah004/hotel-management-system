package com.hms.purchaseservice.controller;

import com.hms.purchaseservice.dto.request.CreateSupplierRequest;
import com.hms.purchaseservice.dto.request.UpdateSupplierRequest;
import com.hms.purchaseservice.dto.response.SupplierResponse;
import com.hms.purchaseservice.entity.SupplierStatus;
import com.hms.purchaseservice.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier Management", description = "Endpoints for managing vendors and suppliers")
public class SupplierController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Register a new supplier")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createSupplier(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Update supplier details")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(purchaseService.updateSupplier(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'HOUSEKEEPER', 'MAINTENANCE')")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getSupplierById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'HOUSEKEEPER', 'MAINTENANCE')")
    @Operation(summary = "Get all suppliers with optional status filter")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers(
            @RequestParam(required = false) SupplierStatus status) {
        return ResponseEntity.ok(purchaseService.getAllSuppliers(status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Delete supplier (or mark inactive if referenced)")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        purchaseService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
