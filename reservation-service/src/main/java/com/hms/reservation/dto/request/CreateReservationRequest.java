package com.hms.reservationservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    @NotNull(message = "Guest ID is required")
    private Long guestId;

    private String resourceType; // ROOM or HALL
    private Long roomId;
    private Long hallId;

    @NotNull(message = "Room category ID is required")
    private Long roomCategoryId;

    @NotNull(message = "Adult count is required")
    @Min(value = 1, message = "At least 1 adult required")
    private Integer adults;

    private Integer children;

    @NotNull(message = "Check-in date/time is required")
    @FutureOrPresent(message = "Check-in cannot be in the past")
    private LocalDateTime checkInDateTime;

    @NotNull(message = "Check-out date/time is required")
    private LocalDateTime checkOutDateTime;

    private String specialRequests;
}
