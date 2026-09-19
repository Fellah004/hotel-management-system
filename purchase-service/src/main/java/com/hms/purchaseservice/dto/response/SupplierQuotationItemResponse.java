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
public class SupplierQuotationItemResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private Integer quotedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal taxPercentage;
    private BigDecimal totalAmount;
}
