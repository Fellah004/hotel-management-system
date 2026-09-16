package com.hms.reservationservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistRequest {

    @NotNull(message = "Guest ID is required")
    private Long guestId;

    @NotNull(message = "Room category ID is required")
    private Long roomCategoryId;

    private String resourceType; // ROOM or HALL

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    private Integer guestsCount;
}
