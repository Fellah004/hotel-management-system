package com.hms.payment.dto;
import jakarta.validation.constraints.*; import lombok.*; import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RefundRequest { @NotNull @DecimalMin("0.01") private BigDecimal amount; @NotBlank private String reason; }