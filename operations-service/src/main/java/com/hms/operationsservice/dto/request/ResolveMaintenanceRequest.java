package com.hms.operationsservice.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolveMaintenanceRequest {

    private BigDecimal cost;

    private String remarks;
}
