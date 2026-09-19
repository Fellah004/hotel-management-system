package com.hms.roomservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating an event / banquet hall")
public class UpdateHallRequest {

    @Schema(description = "Updated descriptive name for the hall", example = "Grand Imperial Ballroom")
    private String name;

    @Schema(description = "Updated Hall Category ID", example = "2")
    private Long categoryId;

    @Schema(description = "Updated floor number", example = "2")
    private Integer floor;

    @Positive(message = "Total area must be positive")
    @Schema(description = "Updated floor area in square feet", example = "5000")
    private Integer totalAreaSqFt;

    @Min(value = 1, message = "Theater capacity must be at least 1")
    @Schema(description = "Updated Theater layout capacity", example = "400")
    private Integer theaterCapacity;

    @Min(value = 1, message = "U-Shape capacity must be at least 1")
    @Schema(description = "Updated U-Shape layout capacity", example = "90")
    private Integer uShapeCapacity;

    @Min(value = 1, message = "Cluster capacity must be at least 1")
    @Schema(description = "Updated Cluster / Round Table layout capacity", example = "250")
    private Integer clusterCapacity;

    @Min(value = 1, message = "Classroom capacity must be at least 1")
    @Schema(description = "Updated Classroom layout capacity", example = "180")
    private Integer classroomCapacity;

    @Positive(message = "Price per hour must be positive")
    @Schema(description = "Updated hourly price", example = "250.00")
    private BigDecimal pricePerHour;

    @Positive(message = "Price per day must be positive")
    @Schema(description = "Updated full-day price", example = "1800.00")
    private BigDecimal pricePerDay;

    @Schema(description = "Updated active status", example = "true")
    private Boolean active;

    @Schema(description = "Updated set of amenity IDs", example = "[1, 2, 3]")
    private Set<Long> amenityIds;
}
