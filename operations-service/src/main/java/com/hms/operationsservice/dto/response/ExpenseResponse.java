package com.hms.operationsservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private Long id;
    private String category;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
    private Long recordedByStaffId;
    private Long approvedByManagerId;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
