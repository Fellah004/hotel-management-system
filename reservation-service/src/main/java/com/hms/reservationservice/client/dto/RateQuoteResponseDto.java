package com.hms.reservationservice.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Detailed rate quote response with night-by-night breakdown")
public class RateQuoteResponseDto {

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Optional room ID", example = "101")
    private Long roomId;

    @Schema(description = "Resource type", example = "ROOM")
    private String resourceType;

    @Schema(description = "Check-in date", example = "2026-09-18")
    private LocalDate checkInDate;

    @Schema(description = "Check-out date", example = "2026-09-20")
    private LocalDate checkOutDate;

    @Schema(description = "Total stay nights", example = "2")
    private int numberOfNights;

    @Schema(description = "Number of guests", example = "1")
    private Integer guestsCount;

    @Schema(description = "Base rate per night", example = "100.00")
    private BigDecimal basePricePerNight;

    @Schema(description = "Total base price before multipliers", example = "200.00")
    private BigDecimal totalBasePrice;

    @Schema(description = "Total dynamic multiplier adjustment", example = "20.00")
    private BigDecimal totalDynamicAdjustment;

    @Schema(description = "Final total quoted price", example = "220.00")
    private BigDecimal totalQuotedAmount;

    @Schema(description = "Night-by-night itemized breakdown")
    private List<DailyRateDetailDto> dailyBreakdown;
}
