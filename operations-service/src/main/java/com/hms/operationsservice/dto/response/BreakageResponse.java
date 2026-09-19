package com.hms.operationsservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakageResponse {
    private Long id;
    private String reportCode;
    private Long reservationId;
    private Long roomId;
    private Long staffId;
    private String description;
    private BigDecimal chargeAmount;
    private String status;
    private Long approvedByManagerId;
    private String remarks;
    private LocalDateTime createdAt;
}
