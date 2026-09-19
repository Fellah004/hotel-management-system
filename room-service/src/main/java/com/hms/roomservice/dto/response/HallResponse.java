package com.hms.roomservice.dto.response;

import com.hms.roomservice.entity.HallStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload representing hall details")
public class HallResponse {

    @Schema(description = "Hall ID", example = "1")
    private Long id;

    @Schema(description = "Hall number / code", example = "HALL-A")
    private String hallNumber;

    @Schema(description = "Hall name", example = "Grand Ballroom")
    private String name;

    @Schema(description = "Category details")
    private HallCategoryResponse category;

    @Schema(description = "Floor location", example = "1")
    private Integer floor;

    @Schema(description = "Total area in sq ft", example = "4500")
    private Integer totalAreaSqFt;

    @Schema(description = "Theater layout capacity", example = "350")
    private Integer theaterCapacity;

    @Schema(description = "U-Shape layout capacity", example = "80")
    private Integer uShapeCapacity;

    @Schema(description = "Cluster layout capacity", example = "200")
    private Integer clusterCapacity;

    @Schema(description = "Classroom layout capacity", example = "150")
    private Integer classroomCapacity;

    @Schema(description = "Hourly price", example = "200.00")
    private BigDecimal pricePerHour;

    @Schema(description = "Full-day price", example = "1500.00")
    private BigDecimal pricePerDay;

    @Schema(description = "Current hall status", example = "AVAILABLE")
    private HallStatus status;

    @Schema(description = "Active flag", example = "true")
    private boolean active;

    @Schema(description = "Set of amenities available in the hall")
    private Set<AmenityResponse> amenities;

    @Schema(description = "Record version for optimistic locking", example = "0")
    private Long version;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
