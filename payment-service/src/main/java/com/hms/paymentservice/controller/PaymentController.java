package com.hms.paymentservice.controller;

import com.hms.paymentservice.dto.request.CreatePaymentRequest;
import com.hms.paymentservice.dto.request.RefundRequest;
import com.hms.paymentservice.dto.response.PaymentResponse;
import com.hms.paymentservice.dto.response.RefundResponse;
import com.hms.paymentservice.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for processing payments and issuing refunds")
@SecurityRequirement(name = "BearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Process a new payment", description = "Processes payment idempotently using idempotencyKey")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return new ResponseEntity<>(paymentService.processPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by primary key")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'GUEST')")
    @Operation(summary = "Get payments by reservation ID", description = "Retrieves all payments associated with a reservation")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentsByReservationId(reservationId));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Process a refund", description = "Issues a partial or full refund for a payment")
    public ResponseEntity<RefundResponse> processRefund(@PathVariable Long id,
                                                        @Valid @RequestBody RefundRequest request) {
        return new ResponseEntity<>(paymentService.processRefund(id, request), HttpStatus.CREATED);
    }
}
