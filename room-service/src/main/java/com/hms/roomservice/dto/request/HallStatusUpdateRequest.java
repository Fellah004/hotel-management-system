package com.hms.roomservice.dto.request;

import com.hms.roomservice.entity.HallStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a hall's status")
public class HallStatusUpdateRequest {

    @NotNull(message = "Hall status is required")
    @Schema(description = "Target hall status", example = "AVAILABLE", allowableValues = {"AVAILABLE", "BOOKED", "SETUP_IN_PROGRESS", "EVENT_ONGOING", "CLEANUP", "MAINTENANCE"})
    private HallStatus status;
}
