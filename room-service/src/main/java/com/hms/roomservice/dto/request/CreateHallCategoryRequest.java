package com.hms.roomservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request payload for creating a hall category")
public class CreateHallCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Schema(description = "Name of the hall category", example = "BANQUET_HALL")
    private String name;

    @Schema(description = "Detailed description of the hall category", example = "Large grand banquet hall suitable for weddings and galas")
    private String description;

    @NotNull(message = "Base price per hour is required")
    @Positive(message = "Base price per hour must be positive")
    @Schema(description = "Base hourly rental rate", example = "150.00")
    private BigDecimal basePricePerHour;

    @NotNull(message = "Base price per day is required")
    @Positive(message = "Base price per day must be positive")
    @Schema(description = "Base full-day rental rate", example = "1200.00")
    private BigDecimal basePricePerDay;

    @NotNull(message = "Minimum capacity is required")
    @Min(value = 1, message = "Minimum capacity must be at least 1")
    @Schema(description = "Minimum guest capacity", example = "50")
    private Integer minCapacity;

    @NotNull(message = "Maximum capacity is required")
    @Min(value = 1, message = "Maximum capacity must be at least 1")
    @Schema(description = "Maximum guest capacity", example = "500")
    private Integer maxCapacity;
}
