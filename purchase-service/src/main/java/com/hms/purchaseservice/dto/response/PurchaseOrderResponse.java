package com.hms.purchaseservice.dto.response;

import com.hms.purchaseservice.entity.OrderStatus;
import com.hms.purchaseservice.entity.PaymentTerms;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id;
    private String orderCode;
    private Long purchaseRequestId;
    private Long quotationId;
    private Long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private OrderStatus status;
    private PaymentTerms paymentTerms;
    private String shippingAddress;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private Long createdByStaffId;
    private Long approvedByManagerId;
    private LocalDateTime approvedAt;
    private String remarks;
    private List<PurchaseOrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
