package com.hms.operationsservice.dto.request;

import com.hms.operationsservice.entity.UtilityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUtilityRequest {

    @NotNull(message = "Utility type is mandatory")
    private UtilityType utilityType;

    @NotNull(message = "Month is mandatory")
    private Integer periodMonth;

    @NotNull(message = "Year is mandatory")
    private Integer periodYear;

    @NotNull(message = "Units consumed is mandatory")
    @Positive(message = "Units must be positive")
    private BigDecimal unitsConsumed;

    @NotNull(message = "Total cost is mandatory")
    @Positive(message = "Cost must be positive")
    private BigDecimal totalCost;

    private String remarks;
}
