package com.hms.paymentservice.dto.response;

import com.hms.paymentservice.entity.PaymentMethod;
import com.hms.paymentservice.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private BigDecimal remainingRefundableAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String providerReference;
    private String idempotencyKey;
    private LocalDateTime paymentTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
