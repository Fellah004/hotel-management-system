package com.hms.billing.dto;
import jakarta.validation.constraints.*; import lombok.*; import java.math.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AdditionalChargeRequest { @NotBlank private String description; @DecimalMin("0.01") private BigDecimal amount; }