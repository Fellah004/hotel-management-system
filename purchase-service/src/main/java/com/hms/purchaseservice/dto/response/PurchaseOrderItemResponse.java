package com.hms.purchaseservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private Integer orderedQuantity;
    private Integer receivedQuantity;
    private Integer remainingQuantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal totalAmount;
}
