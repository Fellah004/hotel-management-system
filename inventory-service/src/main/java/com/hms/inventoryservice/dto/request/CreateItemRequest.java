package com.hms.inventoryservice.dto.request;

import com.hms.inventoryservice.entity.InventoryCategory;
import com.hms.inventoryservice.entity.InventoryUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Category is required")
    private InventoryCategory category;

    private InventoryUnit unit;

    @NotNull(message = "Initial quantity is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer initialQuantity;

    @NotNull(message = "Minimum threshold is required")
    @Min(value = 0, message = "Minimum threshold cannot be negative")
    private Integer minimumThreshold;

    private BigDecimal unitCost;
    private String description;
}
