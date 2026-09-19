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
@Schema(description = "Request payload for rejecting an offered waitlist room")
public class RejectWaitlistRequest {

    @Schema(description = "Optional reason for declining the waitlist offer", example = "Change of travel plans")
    private String reason;
}
