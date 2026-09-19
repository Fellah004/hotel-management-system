package com.hms.purchaseservice.dto.request;

import com.hms.purchaseservice.entity.PaymentTerms;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {

    private Long purchaseRequestId;
    private Long quotationId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;
    private PaymentTerms paymentTerms;
    private String shippingAddress;
    private String remarks;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<PurchaseOrderItemRequest> items;
}
