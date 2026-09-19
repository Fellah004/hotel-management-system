package com.hms.inventoryservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderRuleResponse {
    private Long id;
    private Long itemId;
    private Integer reorderQuantity;
    private String preferredSupplier;
    private boolean autoAlertEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
