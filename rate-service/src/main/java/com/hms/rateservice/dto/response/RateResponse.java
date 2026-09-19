package com.hms.rateservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateResponse {
    private Long id;
    private Long categoryId;
    private String resourceType;
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
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
