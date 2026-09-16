package com.hms.payment.dto;
import com.hms.payment.entity.*; import lombok.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Getter @Setter @Builder
public class PaymentResponse {
 private UUID id; private String paymentNumber; private UUID reservationId; private UUID guestId;
 private BigDecimal amount; private PaymentMethod method; private PaymentStatus status; private String providerReference; private OffsetDateTime createdAt;
}