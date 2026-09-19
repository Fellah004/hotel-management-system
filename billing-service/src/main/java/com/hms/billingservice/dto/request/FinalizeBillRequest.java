package com.hms.billingservice.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalizeBillRequest {
    private BigDecimal paidAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxPercentage; // e.g. 10.0 for 10%
}
