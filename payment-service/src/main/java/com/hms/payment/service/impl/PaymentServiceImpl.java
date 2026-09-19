package com.hms.paymentservice.service.impl;

import com.hms.paymentservice.client.BillingClient;
import com.hms.paymentservice.client.dto.BillDto;
import com.hms.paymentservice.dto.request.CreatePaymentRequest;
import com.hms.paymentservice.dto.request.RefundRequest;
import com.hms.paymentservice.dto.response.PaymentResponse;
import com.hms.paymentservice.dto.response.RefundResponse;
import com.hms.paymentservice.entity.Payment;
import com.hms.paymentservice.entity.PaymentStatus;
import com.hms.paymentservice.entity.Refund;
import com.hms.paymentservice.event.publisher.PaymentEventPublisher;
import com.hms.paymentservice.exception.BusinessRuleException;
import com.hms.paymentservice.exception.ResourceNotFoundException;
import com.hms.paymentservice.repository.PaymentRepository;
import com.hms.paymentservice.repository.RefundRepository;
import com.hms.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final BillingClient billingClient;

    @Override
    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest request) {
        log.info("Processing payment for reservationId: {}, amount: {}, method: {}, idempotencyKey: {}",
                request.getReservationId(), request.getAmount(), request.getPaymentMethod(), request.getIdempotencyKey());

        // 1. Idempotency check
        Optional<Payment> existingOpt = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            log.info("Idempotent payment match found for key: {}. Returning existing record id: {}",
                    request.getIdempotencyKey(), existingOpt.get().getId());
            return mapToPaymentResponse(existingOpt.get());
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be strictly positive");
        }

        // 2. Strict Bill Finalization Verification via billing-service
        BillDto bill = null;
        try {
            bill = billingClient.getBillByReservationId(request.getReservationId());
        } catch (Exception e) {
            log.warn("Could not retrieve bill for reservationId {}: {}", request.getReservationId(), e.getMessage());
        }

        if (bill == null) {
            throw new BusinessRuleException("No bill found for reservation ID: " + request.getReservationId() +
                    ". Final bill must be generated before making payment.");
        }

        if ("PENDING".equalsIgnoreCase(bill.getStatus())) {
            throw new BusinessRuleException("Payment cannot be processed. The bill for reservation ID " +
                    request.getReservationId() + " is still PENDING. The final bill must be generated/finalized first.");
        }

        if ("CANCELLED".equalsIgnoreCase(bill.getStatus())) {
            throw new BusinessRuleException("Cannot process payment for a cancelled bill (reservation ID: " +
                    request.getReservationId() + ").");
        }

        if ("PAID".equalsIgnoreCase(bill.getStatus()) ||
                (bill.getOutstandingAmount() != null && bill.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new BusinessRuleException("The bill for reservation ID " + request.getReservationId() +
                    " is already fully paid.");
        }

        if (bill.getOutstandingAmount() != null && request.getAmount().compareTo(bill.getOutstandingAmount()) > 0) {
            throw new BusinessRuleException("Payment amount (" + request.getAmount() +
                    ") exceeds remaining outstanding balance (" + bill.getOutstandingAmount() + ").");
        }

        String providerRef = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .reservationId(request.getReservationId())
                .amount(request.getAmount())
                .refundedAmount(BigDecimal.ZERO)
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.SUCCESS)
                .providerReference(providerRef)
                .idempotencyKey(request.getIdempotencyKey())
                .paymentTime(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment successfully processed with id: {} for reservation: {}", savedPayment.getId(), request.getReservationId());

        // Publish PaymentSucceeded event
        paymentEventPublisher.publishPaymentSucceeded(savedPayment);

        return mapToPaymentResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RefundResponse processRefund(Long paymentId, RefundRequest request) {
        log.info("Processing refund for paymentId: {}, refundAmount: {}, reason: {}",
                paymentId, request.getAmount(), request.getReason());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BusinessRuleException("Cannot refund a payment with status: " + payment.getStatus());
        }

        BigDecimal remainingEligible = payment.getAmount().subtract(payment.getRefundedAmount());
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Refund amount must be strictly positive");
        }

        if (request.getAmount().compareTo(remainingEligible) > 0) {
            throw new BusinessRuleException("Refund amount " + request.getAmount() +
                    " exceeds remaining refundable amount " + remainingEligible);
        }

        Refund refund = Refund.builder()
                .paymentId(payment.getId())
                .reservationId(payment.getReservationId())
                .amount(request.getAmount())
                .reason(request.getReason())
                .status("COMPLETED")
                .build();

        Refund savedRefund = refundRepository.save(refund);

        // Update payment refundedAmount and status
        BigDecimal newRefunded = payment.getRefundedAmount().add(request.getAmount());
        payment.setRefundedAmount(newRefunded);
        if (newRefunded.compareTo(payment.getAmount()) >= 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        paymentRepository.save(payment);

        log.info("Refund created with id: {} for paymentId: {}, new payment status: {}",
                savedRefund.getId(), payment.getId(), payment.getStatus());

        // Publish RefundCompleted event
        paymentEventPublisher.publishRefundCompleted(savedRefund);

        return RefundResponse.builder()
                .id(savedRefund.getId())
                .paymentId(savedRefund.getPaymentId())
                .amount(savedRefund.getAmount())
                .reason(savedRefund.getReason())
                .createdAt(savedRefund.getCreatedAt())
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .reservationId(payment.getReservationId())
                .amount(payment.getAmount())
                .refundedAmount(payment.getRefundedAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .providerReference(payment.getProviderReference())
                .idempotencyKey(payment.getIdempotencyKey())
                .paymentTime(payment.getPaymentTime())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
