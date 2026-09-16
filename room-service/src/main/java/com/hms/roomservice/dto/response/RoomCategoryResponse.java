package com.hms.roomservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer maxOccupancy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
