package com.hms.purchaseservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseAuditLogResponse {
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String oldState;
    private String newState;
    private String performedBy;
    private String remarks;
    private LocalDateTime createdAt;
}
