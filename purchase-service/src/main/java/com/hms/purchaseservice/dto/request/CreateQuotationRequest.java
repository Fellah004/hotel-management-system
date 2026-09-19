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
public class CreateQuotationRequest {

    @NotNull(message = "Purchase Request ID is required")
    private Long purchaseRequestId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Quotation date is required")
    private LocalDate quotationDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private PaymentTerms paymentTerms;
    private Integer deliveryLeadTimeDays;
    private String notes;

    @NotEmpty(message = "Quotation items cannot be empty")
    @Valid
    private List<QuotationItemRequest> items;
}
