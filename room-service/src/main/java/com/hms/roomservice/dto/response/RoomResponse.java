package com.hms.roomservice.dto.response;

import com.hms.roomservice.entity.ResourceType;
import com.hms.roomservice.entity.RoomStatus;
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
public class RoomResponse {
    private Long id;
    private String roomNumber;
    private RoomCategoryResponse category;
    private ResourceType resourceType;
    private Integer floor;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private RoomStatus status;
    private boolean active;
    private Set<AmenityResponse> amenities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
