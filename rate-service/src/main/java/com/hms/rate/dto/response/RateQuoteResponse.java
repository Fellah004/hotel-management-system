package com.hms.rateservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateQuoteResponse {
    private Long categoryId;
    private Long roomId;
    private String resourceType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfNights;
    private Integer guestsCount;
    private BigDecimal basePricePerNight;
    private BigDecimal totalBasePrice;
    private BigDecimal totalDynamicAdjustment;
    private BigDecimal totalQuotedAmount;
    private List<DailyRateDetail> dailyBreakdown;
}
