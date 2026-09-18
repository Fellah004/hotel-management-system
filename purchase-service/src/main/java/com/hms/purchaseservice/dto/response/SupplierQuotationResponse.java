package com.hms.purchaseservice.dto.response;

import com.hms.purchaseservice.entity.PaymentTerms;
import com.hms.purchaseservice.entity.QuotationStatus;
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
public class SupplierQuotationResponse {
    private Long id;
    private String quoteCode;
    private Long purchaseRequestId;
    private Long supplierId;
    private String supplierName;
    private LocalDate quotationDate;
    private LocalDate expiryDate;
    private PaymentTerms paymentTerms;
    private Integer deliveryLeadTimeDays;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private QuotationStatus status;
    private String notes;
    private List<SupplierQuotationItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
