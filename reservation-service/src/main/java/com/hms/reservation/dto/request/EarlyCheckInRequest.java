package com.hms.reservationservice.dto.request;

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
public class EarlyCheckInRequest {

    @NotNull(message = "Requested check-in time is required")
    private LocalDateTime requestedCheckInTime;

    private String reason;
}
