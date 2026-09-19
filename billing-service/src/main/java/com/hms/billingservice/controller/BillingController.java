package com.hms.billingservice.controller;

import com.hms.billingservice.dto.request.AddBillItemRequest;
import com.hms.billingservice.dto.request.CreateBillRequest;
import com.hms.billingservice.dto.request.FinalizeBillRequest;
import com.hms.billingservice.dto.response.BillItemResponse;
import com.hms.billingservice.dto.response.BillResponse;
import com.hms.billingservice.dto.response.PrintBillResponse;
import com.hms.billingservice.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Billing Management", description = "APIs for bills, invoices, itemized charges, and finalization")
@SecurityRequirement(name = "BearerAuth")
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Create a new bill", description = "Initializes an itemized bill for a reservation")
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody CreateBillRequest request) {
        return new ResponseEntity<>(billingService.createBill(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get bill by ID", description = "Retrieves bill and line items by primary key")
    public ResponseEntity<BillResponse> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getBillById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get bill by reservation ID", description = "Retrieves bill details for a reservation")
    public ResponseEntity<BillResponse> getBillByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(billingService.getBillByReservationId(reservationId));
    }

    @PostMapping("/reservation/{reservationId}/record-payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Record payment for reservation", description = "Updates bill paidAmount and sets status to PAID when balance reaches 0")
    public ResponseEntity<BillResponse> recordPaymentForReservation(
            @PathVariable Long reservationId,
            @RequestParam java.math.BigDecimal amount) {
        return ResponseEntity.ok(billingService.recordPaymentForReservation(reservationId, amount));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Add item to bill", description = "Adds a line item (room charge, service, damage, etc.) to the bill")
    public ResponseEntity<BillItemResponse> addItemToBill(@PathVariable Long id,
                                                          @Valid @RequestBody AddBillItemRequest request) {
        return new ResponseEntity<>(billingService.addItemToBill(id, request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Calculate bill", description = "Recalculates subtotal, taxes, discounts, and outstanding amounts")
    public ResponseEntity<BillResponse> calculateBill(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.calculateBill(id));
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Finalize bill", description = "Locks the bill from future modification and publishes BillFinalized event")
    public ResponseEntity<BillResponse> finalizeBill(@PathVariable Long id,
                                                     @RequestBody(required = false) FinalizeBillRequest request) {
        return ResponseEntity.ok(billingService.finalizeBill(id, request));
    }

    @GetMapping("/{id}/print")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Print bill representation", description = "Returns a structured printable receipt/invoice")
    public ResponseEntity<PrintBillResponse> getPrintBill(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getPrintBill(id));
    }
}
