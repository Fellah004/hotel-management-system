package com.hms.reportingservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal roomRevenue;
    private BigDecimal hallRevenue;
    private BigDecimal serviceRevenue;
    private BigDecimal otherRevenue;
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> revenueByDay;
}
