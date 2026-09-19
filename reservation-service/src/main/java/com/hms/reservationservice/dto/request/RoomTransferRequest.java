package com.hms.reservationservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTransferRequest {

    @NotNull(message = "Target room ID is required")
    private Long targetRoomId;

    private String reason;
}
