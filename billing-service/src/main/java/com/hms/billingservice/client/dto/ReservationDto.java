package com.hms.billingservice.client.dto;

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
public class ReservationDto {
    private Long id;
    private String reservationCode;
    private Long guestId;
    private String resourceType;
    private Long roomId;
    private Long hallId;
    private Long roomCategoryId;
    private Long hallCategoryId;
    private Integer adults;
    private Integer children;
    private LocalDateTime checkInDateTime;
    private LocalDateTime checkOutDateTime;
    private Integer nights;
    private BigDecimal quotedAmount;
    private String status;
}
