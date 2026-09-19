package com.hms.roomservice.dto.request;

import com.hms.roomservice.entity.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusUpdateRequest {
    @NotNull(message = "New status is required")
    private RoomStatus status;

    private String reason;
}
