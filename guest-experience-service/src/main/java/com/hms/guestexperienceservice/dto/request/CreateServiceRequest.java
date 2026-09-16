package com.hms.guestexperienceservice.dto.request;

import com.hms.guestexperienceservice.entity.RequestType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateServiceRequest {

    @NotNull(message = "Reservation ID is mandatory")
    private Long reservationId;

    @NotNull(message = "Guest ID is mandatory")
    private Long guestId;

    @NotNull(message = "Room ID is mandatory")
    private Long roomId;

    @NotNull(message = "Request type is mandatory")
    private RequestType requestType;

    private String description;

    @Builder.Default
    private Integer quantity = 1;

    @Builder.Default
    private String priority = "NORMAL";
}
