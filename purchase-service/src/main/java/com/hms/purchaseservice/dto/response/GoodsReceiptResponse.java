package com.hms.purchaseservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptResponse {
    private Long id;
    private String receiptCode;
    private Long purchaseOrderId;
    private String deliveryNoteNumber;
    private LocalDate receivedDate;
    private Long receivedByStaffId;
    private String vehicleNumber;
    private String remarks;
    private List<GoodsReceiptItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
