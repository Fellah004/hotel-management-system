package com.hms.reservationservice.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Itemized daily rate calculation detail")
public class DailyRateDetailDto {

    @Schema(description = "Stay night date", example = "2026-09-18")
    private LocalDate date;

    @Schema(description = "Day of week", example = "FRIDAY")
    private String dayOfWeek;

    @Schema(description = "Base price per night for this category", example = "100.00")
    private BigDecimal basePrice;

    @Schema(description = "Applied weekend multiplier", example = "1.00")
    private BigDecimal weekendMultiplier;

    @Schema(description = "Applied holiday multiplier", example = "1.00")
    private BigDecimal holidayMultiplier;

    @Schema(description = "Applied seasonal multiplier", example = "1.00")
    private BigDecimal seasonalMultiplier;

    @Schema(description = "Applied occupancy demand multiplier", example = "1.00")
    private BigDecimal occupancyMultiplier;

    @Schema(description = "Final calculated price for this individual night", example = "100.00")
    private BigDecimal finalDailyPrice;
}
