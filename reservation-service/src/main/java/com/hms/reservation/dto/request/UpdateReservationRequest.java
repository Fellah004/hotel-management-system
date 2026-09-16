package com.hms.reservationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationRequest {
    private LocalDateTime checkInDateTime;
    private LocalDateTime checkOutDateTime;
    private Integer adults;
    private Integer children;
    private String specialRequests;
}
