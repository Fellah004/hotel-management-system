package com.hms.billingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBillRequest {

    @NotNull(message = "Reservation ID is mandatory")
    private Long reservationId;

    @NotNull(message = "Guest ID is mandatory")
    private Long guestId;

    private Long roomId;
    private Long hallId;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
