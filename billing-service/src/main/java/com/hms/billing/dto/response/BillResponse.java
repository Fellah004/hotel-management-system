package com.hms.billingservice.dto.response;

import com.hms.billingservice.entity.BillStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {
    private Long id;
    private String billingNumber;
    private Long reservationId;
    private Long guestId;
    private Long roomId;
    private Long hallId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal breakageCharges;
    private BigDecimal lateCheckoutCharges;
    private BigDecimal upgradeCharges;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private BillStatus status;
    private List<BillItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime finalizedAt;
}
