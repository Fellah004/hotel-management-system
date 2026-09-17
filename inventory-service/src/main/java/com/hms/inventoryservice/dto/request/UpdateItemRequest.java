package com.hms.inventoryservice.dto.request;

import com.hms.inventoryservice.entity.InventoryCategory;
import com.hms.inventoryservice.entity.InventoryUnit;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {
    private String name;
    private InventoryCategory category;
    private InventoryUnit unit;

    @Min(value = 0, message = "Minimum threshold cannot be negative")
    private Integer minimumThreshold;

    private BigDecimal unitCost;
    private String description;
    private Boolean active;
}
