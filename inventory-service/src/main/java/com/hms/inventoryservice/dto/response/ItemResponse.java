package com.hms.inventoryservice.dto.response;

import com.hms.inventoryservice.entity.InventoryCategory;
import com.hms.inventoryservice.entity.InventoryUnit;
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
    private InventoryCategory category;
    private InventoryUnit unit;
    private Integer quantityInStock;
    private Integer minimumThreshold;
    private BigDecimal unitCost;
    private String description;
    private boolean lowStock;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
