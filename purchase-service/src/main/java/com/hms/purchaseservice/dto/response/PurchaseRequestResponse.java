package com.hms.purchaseservice.dto.response;

import com.hms.purchaseservice.entity.PriorityLevel;
import com.hms.purchaseservice.entity.RequestStatus;
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
public class PurchaseRequestResponse {
    private Long id;
    private String requestCode;
    private Long departmentId;
    private Long requesterId;
    private String requesterRole;
    private String reason;
    private PriorityLevel priority;
    private RequestStatus status;
    private LocalDate requiredByDate;
    private Long approvedByManagerId;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private List<PurchaseRequestItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
