package com.hms.inventoryservice.dto.response;

import com.hms.inventoryservice.entity.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long id;
    private Long itemId;
    private MovementType movementType;
    private Integer quantity;
    private Integer balanceAfter;
    private String reason;
    private String referenceCode;
    private Long staffId;
    private LocalDateTime createdAt;
}
