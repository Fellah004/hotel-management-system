package com.hms.operationsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenCashDrawerRequest {

    @NotNull(message = "Drawer date is mandatory")
    private LocalDate drawerDate;

    private Long shiftId;

    @NotNull(message = "Staff ID is mandatory")
    private Long staffId;

    @NotNull(message = "Opening balance is mandatory")
    @PositiveOrZero(message = "Opening balance must be non-negative")
    private BigDecimal openingBalance;

    private String remarks;
}
