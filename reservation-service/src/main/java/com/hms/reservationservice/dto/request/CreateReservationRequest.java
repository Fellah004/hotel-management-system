package com.hms.reservationservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload for creating a room or hall reservation")
public class CreateReservationRequest {

    @NotNull(message = "Guest ID is required")
    @Schema(description = "ID of the guest creating the reservation", example = "1")
    private Long guestId;

    @Schema(description = "Type of resource being reserved: ROOM or HALL", example = "ROOM", allowableValues = {"ROOM", "HALL"})
    private String resourceType; // ROOM or HALL

    @Schema(description = "Optional pre-assigned Room ID (for room reservations)", example = "1")
    private Long roomId;

    @Schema(description = "Optional pre-assigned Hall ID (for hall reservations)", example = "1")
    private Long hallId;

    @Schema(description = "Room Category ID (required if resourceType is ROOM and roomId is not specified)", example = "1")
    private Long roomCategoryId;

    @Schema(description = "Hall Category ID (required if resourceType is HALL and hallId is not specified)", example = "1")
    private Long hallCategoryId;

    @NotNull(message = "Adult count is required")
    @Min(value = 1, message = "At least 1 adult required")
    @Schema(description = "Number of adult guests / attendees", example = "2")
    private Integer adults;

    @Schema(description = "Number of child guests", example = "0")
    private Integer children;

    @NotNull(message = "Check-in date/time is required")
    @FutureOrPresent(message = "Check-in cannot be in the past")
    @Schema(description = "Check-in / Event start date-time", example = "2026-10-01T14:00:00")
    private LocalDateTime checkInDateTime;

    @NotNull(message = "Check-out date/time is required")
    @Schema(description = "Check-out / Event end date-time", example = "2026-10-03T11:00:00")
    private LocalDateTime checkOutDateTime;

    @Schema(description = "Special guest or event requests", example = "Late check-in requested / Projector setup needed")
    private String specialRequests;
}
