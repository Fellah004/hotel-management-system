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
@Schema(description = "Request payload for confirming a waitlist offer")
public class ConfirmWaitlistRequest {

    @Schema(description = "Optional room ID to confirm for this waitlist", example = "101")
    private Long roomId;
}
