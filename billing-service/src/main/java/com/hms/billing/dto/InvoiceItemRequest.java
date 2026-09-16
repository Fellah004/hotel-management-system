package com.hms.billing.dto;
import jakarta.validation.constraints.*; import lombok.*; import java.math.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InvoiceItemRequest { @NotBlank private String description; @Min(1) private Integer quantity; @DecimalMin("0.00") private BigDecimal unitPrice; }