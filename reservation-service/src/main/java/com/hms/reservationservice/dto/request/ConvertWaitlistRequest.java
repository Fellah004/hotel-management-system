package com.hms.reservationservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for converting a waitlist entry into a reservation")
public class ConvertWaitlistRequest {

    @Schema(description = "Optional room ID to assign to the new reservation", example = "101")
    private Long roomId;

    @Schema(description = "Special requests for the reservation", example = "Early check-in requested")
    private String specialRequests;
}
