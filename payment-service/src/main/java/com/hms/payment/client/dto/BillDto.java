package com.hms.paymentservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDto {
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
    private String status; // PENDING, FINALIZED, PAID, CANCELLED
}
