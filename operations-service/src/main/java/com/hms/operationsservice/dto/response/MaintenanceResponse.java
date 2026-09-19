package com.hms.operationsservice.dto.response;

import com.hms.operationsservice.entity.MaintenanceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceResponse {
    private Long id;
    private String issueCode;
    private Long roomId;
    private String description;
    private MaintenanceStatus status;
    private Long assignedStaffId;
    private BigDecimal cost;
    private String reportedBy;
    private LocalDateTime resolvedAt;
    private Long verifiedByManagerId;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
