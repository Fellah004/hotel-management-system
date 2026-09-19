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
public class RoomDowngradeRequest {

    @NotNull(message = "Target category ID is required")
    private Long targetCategoryId;

    private Long targetRoomId;
    private String reason;
}
