package com.hms.operationsservice.dto.response;

import com.hms.operationsservice.entity.UtilityType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityResponse {
    private Long id;
    private UtilityType utilityType;
    private Integer periodMonth;
    private Integer periodYear;
    private BigDecimal unitsConsumed;
    private BigDecimal totalCost;
    private String remarks;
    private LocalDateTime recordedAt;
}
