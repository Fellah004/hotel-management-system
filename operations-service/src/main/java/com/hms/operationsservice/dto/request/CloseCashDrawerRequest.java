package com.hms.operationsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloseCashDrawerRequest {

    @NotNull(message = "Staff ID is mandatory")
    private Long staffId;

    @NotNull(message = "Cash received is mandatory")
    @PositiveOrZero(message = "Cash received must be non-negative")
    private BigDecimal cashReceived;

    @NotNull(message = "Cash expenses is mandatory")
    @PositiveOrZero(message = "Cash expenses must be non-negative")
    private BigDecimal cashExpenses;

    @NotNull(message = "Actual closing cash is mandatory")
    @PositiveOrZero(message = "Actual closing cash must be non-negative")
    private BigDecimal actualClosing;

    private String remarks;
}
