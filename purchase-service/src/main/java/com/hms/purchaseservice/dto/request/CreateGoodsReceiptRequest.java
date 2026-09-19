package com.hms.purchaseservice.dto.request;

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
public class CreateGoodsReceiptRequest {

    @NotNull(message = "Purchase Order ID is required")
    private Long purchaseOrderId;

    private String deliveryNoteNumber;

    @NotNull(message = "Received date is required")
    private LocalDate receivedDate;

    @NotNull(message = "Received by staff ID is required")
    private Long receivedByStaffId;

    private String vehicleNumber;
    private String remarks;

    @NotEmpty(message = "Received line items cannot be empty")
    @Valid
    private List<GoodsReceiptItemRequest> items;
}
