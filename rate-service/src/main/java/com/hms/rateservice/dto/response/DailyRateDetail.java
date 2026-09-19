package com.hms.rateservice.dto.response;

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
public class DailyRateDetail {
    private LocalDate date;
    private String dayOfWeek;
    private BigDecimal basePrice;
    private BigDecimal weekendMultiplier;
    private BigDecimal holidayMultiplier;
    private BigDecimal seasonalMultiplier;
    private BigDecimal occupancyMultiplier;
    private BigDecimal finalDailyPrice;
}
