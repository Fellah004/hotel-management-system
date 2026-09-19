package com.hms.purchaseservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemResponse {
    private Long id;
    private Long purchaseOrderItemId;
    private Long itemId;
    private Integer receivedQuantity;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private String rejectionReason;
    private String batchNumber;
    private LocalDate expiryDate;
}
