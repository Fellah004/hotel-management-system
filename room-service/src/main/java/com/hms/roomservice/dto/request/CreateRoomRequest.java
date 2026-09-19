package com.hms.roomservice.dto.request;

import com.hms.roomservice.entity.ResourceType;
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
@Schema(description = "Request payload for creating a lodging room")
public class CreateRoomRequest {

    @NotBlank(message = "Room number is required")
    @Schema(description = "Room number (e.g., 101, 201)", example = "101")
    private String roomNumber;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Room Category ID", example = "1")
    private Long categoryId;

    @Schema(hidden = true)
    private ResourceType resourceType;

    @NotNull(message = "Floor is required")
    @Schema(description = "Floor number", example = "1")
    private Integer floor;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(description = "Guest capacity", example = "2")
    private Integer capacity;

    @NotNull(message = "Price per night is required")
    @Positive(message = "Price per night must be positive")
    @Schema(description = "Price per night", example = "150.00")
    private BigDecimal pricePerNight;

    @Schema(description = "Set of amenity IDs", example = "[1, 2]")
    private Set<Long> amenityIds;
}
