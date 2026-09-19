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
public class ExpenseReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalExpenses;
    private Map<String, BigDecimal> expensesByCategory;
}
