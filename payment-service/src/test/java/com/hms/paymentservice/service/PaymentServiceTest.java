package com.hms.paymentservice.service;

import com.hms.paymentservice.dto.request.CreatePaymentRequest;
import com.hms.paymentservice.dto.request.RefundRequest;
import com.hms.paymentservice.dto.response.PaymentResponse;
import com.hms.paymentservice.dto.response.RefundResponse;
import com.hms.paymentservice.entity.Payment;
import com.hms.paymentservice.entity.PaymentMethod;
import com.hms.paymentservice.entity.PaymentStatus;
import com.hms.paymentservice.entity.Refund;
import com.hms.paymentservice.event.publisher.PaymentEventPublisher;
import com.hms.paymentservice.exception.BusinessRuleException;
import com.hms.paymentservice.repository.PaymentRepository;
import com.hms.paymentservice.repository.RefundRepository;
import com.hms.paymentservice.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private CreatePaymentRequest createPaymentRequest;
    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        createPaymentRequest = CreatePaymentRequest.builder()
                .reservationId(100L)
                .amount(new BigDecimal("250.00"))
                .paymentMethod(PaymentMethod.CARD)
                .idempotencyKey("IDEM-12345")
                .build();

        samplePayment = Payment.builder()
                .id(1L)
                .reservationId(100L)
                .amount(new BigDecimal("250.00"))
                .refundedAmount(BigDecimal.ZERO)
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .providerReference("PAY-123")
                .idempotencyKey("IDEM-12345")
                .paymentTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should process new payment and publish event")
    void testProcessPayment_Success() {
        when(paymentRepository.findByIdempotencyKey("IDEM-12345")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(samplePayment);

        PaymentResponse response = paymentService.processPayment(createPaymentRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("250.00"), response.getAmount());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentEventPublisher, times(1)).publishPaymentSucceeded(any(Payment.class));
    }

    @Test
    @DisplayName("Should return existing payment idempotently when key is already processed")
    void testProcessPayment_Idempotent() {
        when(paymentRepository.findByIdempotencyKey("IDEM-12345")).thenReturn(Optional.of(samplePayment));

        PaymentResponse response = paymentService.processPayment(createPaymentRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("IDEM-12345", response.getIdempotencyKey());

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentEventPublisher, never()).publishPaymentSucceeded(any());
    }

    @Test
    @DisplayName("Should process partial refund successfully")
    void testProcessRefund_PartialSuccess() {
        RefundRequest refundRequest = RefundRequest.builder()
                .amount(new BigDecimal("100.00"))
                .reason("Guest early check-out")
                .build();

        Refund sampleRefund = Refund.builder()
                .id(10L)
                .paymentId(1L)
                .reservationId(100L)
                .amount(new BigDecimal("100.00"))
                .reason("Guest early check-out")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));
        when(refundRepository.save(any(Refund.class))).thenReturn(sampleRefund);
        when(paymentRepository.save(any(Payment.class))).thenReturn(samplePayment);

        RefundResponse response = paymentService.processRefund(1L, refundRequest);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());

        verify(refundRepository, times(1)).save(any(Refund.class));
        verify(paymentEventPublisher, times(1)).publishRefundCompleted(any(Refund.class));
    }

    @Test
    @DisplayName("Should fail refund when amount exceeds paid amount")
    void testProcessRefund_ExceedsPaidAmount() {
        RefundRequest refundRequest = RefundRequest.builder()
                .amount(new BigDecimal("500.00"))
                .reason("Excessive refund")
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));

        assertThrows(BusinessRuleException.class, () -> paymentService.processRefund(1L, refundRequest));
        verify(refundRepository, never()).save(any(Refund.class));
    }
}
