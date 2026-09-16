package com.hms.billing.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import lombok.*; import java.math.*; import java.util.*; import java.util.UUID;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InvoiceRequest {
 @NotNull private UUID reservationId; @NotNull private UUID guestId;
 @NotEmpty(message="At least one invoice line item is required") @Valid private List<InvoiceItemRequest> items;
 @DecimalMin("0.00") private BigDecimal taxRate=BigDecimal.ZERO;
 @DecimalMin("0.00") private BigDecimal discountAmount=BigDecimal.ZERO;
}