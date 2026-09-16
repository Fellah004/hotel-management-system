package com.hms.rateservice.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRateRequest {
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    private BigDecimal firstNightPrice;
    private BigDecimal extensionPrice;
    private BigDecimal weekendMultiplier;
    private BigDecimal holidayMultiplier;
    private BigDecimal seasonalMultiplier;
    private BigDecimal occupancyThreshold;
    private BigDecimal occupancyMultiplier;
    private Integer lastMinuteDays;
    private BigDecimal lastMinuteMultiplier;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;
}
