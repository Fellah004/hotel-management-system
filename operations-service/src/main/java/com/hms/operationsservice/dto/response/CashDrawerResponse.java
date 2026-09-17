package com.hms.operationsservice.dto.response;

import com.hms.operationsservice.entity.CashDrawerStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashDrawerResponse {
    private Long id;
    private LocalDate drawerDate;
    private Long shiftId;
    private Long openedByStaffId;
    private Long closedByStaffId;
    private BigDecimal openingBalance;
    private BigDecimal cashReceived;
    private BigDecimal cashExpenses;
    private BigDecimal expectedClosing;
    private BigDecimal actualClosing;
    private BigDecimal difference;
    private CashDrawerStatus status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
