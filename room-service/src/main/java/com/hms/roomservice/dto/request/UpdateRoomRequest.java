package com.hms.roomservice.dto.request;

import com.hms.roomservice.entity.ResourceType;
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
public class UpdateRoomRequest {
    private Long categoryId;
    private ResourceType resourceType;
    private Integer floor;
    private Integer capacity;

    @Positive(message = "Price per night must be positive")
    private BigDecimal pricePerNight;

    private Boolean active;
    private Set<Long> amenityIds;
}
