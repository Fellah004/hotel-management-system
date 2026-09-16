package com.hms.payment.dto;
import com.hms.payment.entity.PaymentMethod; import jakarta.validation.constraints.*; import lombok.*; import java.math.BigDecimal; import java.util.UUID;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentRequest {
 @NotNull private UUID reservationId; @NotNull private UUID guestId;
 @NotNull @DecimalMin("0.01") private BigDecimal amount;
 @NotNull private PaymentMethod method;
 private String providerReference; private String idempotencyKey;
}