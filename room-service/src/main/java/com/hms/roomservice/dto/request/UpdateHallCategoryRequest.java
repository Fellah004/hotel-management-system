package com.hms.roomservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a hall category")
public class UpdateHallCategoryRequest {

    @Schema(description = "Updated name of the hall category", example = "GRAND_BANQUET_HALL")
    private String name;

    @Schema(description = "Updated description of the hall category")
    private String description;

    @Positive(message = "Base price per hour must be positive")
    @Schema(description = "Updated base hourly rental rate", example = "175.00")
    private BigDecimal basePricePerHour;

    @Positive(message = "Base price per day must be positive")
    @Schema(description = "Updated base full-day rental rate", example = "1400.00")
    private BigDecimal basePricePerDay;

    @Min(value = 1, message = "Minimum capacity must be at least 1")
    @Schema(description = "Updated minimum capacity", example = "60")
    private Integer minCapacity;

    @Min(value = 1, message = "Maximum capacity must be at least 1")
    @Schema(description = "Updated maximum capacity", example = "600")
    private Integer maxCapacity;
}
