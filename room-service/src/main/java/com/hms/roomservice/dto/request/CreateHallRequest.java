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
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating an event / banquet hall")
public class CreateHallRequest {

    @NotBlank(message = "Hall number / code is required")
    @Schema(description = "Unique hall number or identifier", example = "HALL-A")
    private String hallNumber;

    @NotBlank(message = "Hall name is required")
    @Schema(description = "Descriptive name for the hall", example = "Grand Ballroom")
    private String name;

    @NotNull(message = "Hall Category ID is required")
    @Schema(description = "ID of the associated Hall Category", example = "1")
    private Long categoryId;

    @NotNull(message = "Floor is required")
    @Schema(description = "Floor location of the hall", example = "1")
    private Integer floor;

    @NotNull(message = "Total area (sq ft) is required")
    @Positive(message = "Total area must be positive")
    @Schema(description = "Floor area in square feet", example = "4500")
    private Integer totalAreaSqFt;

    @NotNull(message = "Theater capacity is required")
    @Min(value = 1, message = "Theater capacity must be at least 1")
    @Schema(description = "Max seating capacity in Theater layout", example = "350")
    private Integer theaterCapacity;

    @NotNull(message = "U-Shape capacity is required")
    @Min(value = 1, message = "U-Shape capacity must be at least 1")
    @Schema(description = "Max seating capacity in U-Shape layout", example = "80")
    private Integer uShapeCapacity;

    @NotNull(message = "Cluster capacity is required")
    @Min(value = 1, message = "Cluster capacity must be at least 1")
    @Schema(description = "Max seating capacity in Round-Table / Cluster layout", example = "200")
    private Integer clusterCapacity;

    @NotNull(message = "Classroom capacity is required")
    @Min(value = 1, message = "Classroom capacity must be at least 1")
    @Schema(description = "Max seating capacity in Classroom layout", example = "150")
    private Integer classroomCapacity;

    @NotNull(message = "Price per hour is required")
    @Positive(message = "Price per hour must be positive")
    @Schema(description = "Hourly rental price", example = "200.00")
    private BigDecimal pricePerHour;

    @NotNull(message = "Price per day is required")
    @Positive(message = "Price per day must be positive")
    @Schema(description = "Full-day rental price", example = "1500.00")
    private BigDecimal pricePerDay;

    @Schema(description = "Set of amenity / equipment IDs to link with this hall", example = "[1, 2]")
    private Set<Long> amenityIds;
}
