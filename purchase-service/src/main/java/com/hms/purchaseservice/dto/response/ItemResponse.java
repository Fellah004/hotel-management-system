package com.hms.purchaseservice.dto.response;

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
public class ItemResponse {
    private Long id;
    private String itemCode;
    private String name;
    private String description;
    private String category;
    private Integer currentStock;
    private Integer minThreshold;
    private Integer maxCapacity;
    private String unit;
    private BigDecimal unitCost;
    private String location;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
