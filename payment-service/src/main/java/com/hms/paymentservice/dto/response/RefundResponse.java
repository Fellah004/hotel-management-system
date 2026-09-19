package com.hms.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Long id;
    private Long paymentId;
    private Long reservationId;
    private BigDecimal amount;
    private String reason;
    private String status;
    private Long refundedByStaffId;
    private LocalDateTime createdAt;
}
