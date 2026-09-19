package com.hms.purchaseservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemRequest {

    @NotNull(message = "Purchase Order Item ID is required")
    private Long purchaseOrderItemId;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Received quantity is required")
    @Min(value = 0, message = "Received quantity cannot be negative")
    private Integer receivedQuantity;

    @NotNull(message = "Accepted quantity is required")
    @Min(value = 0, message = "Accepted quantity cannot be negative")
    private Integer acceptedQuantity;

    private Integer rejectedQuantity;
    private String rejectionReason;
    private String batchNumber;
    private LocalDate expiryDate;
}
