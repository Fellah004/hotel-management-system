package com.hms.operationsservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportBreakageRequest {

    private Long reservationId;
    private Long roomId;

    @NotNull(message = "Staff ID is mandatory")
    private Long staffId;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotNull(message = "Charge amount is mandatory")
    @Positive(message = "Charge amount must be positive")
    private BigDecimal chargeAmount;
}
