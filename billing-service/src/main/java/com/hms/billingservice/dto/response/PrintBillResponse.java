package com.hms.billingservice.dto.response;

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
public class PrintBillResponse {
    private String billingNumber;
    private Long guestId;
    private Long reservationId;
    private Long roomId;
    private Long hallId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<BillItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal breakageCharges;
    private BigDecimal lateCheckoutCharges;
    private BigDecimal upgradeCharges;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private String status;
    private LocalDateTime invoiceDate;
    private LocalDateTime finalizedAt;
    private String hotelHeader;
    private String footerNotes;
}
