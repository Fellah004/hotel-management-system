package com.hms.guestexperienceservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemLoyaltyRequest {

    @NotNull(message = "Guest ID is mandatory")
    private Long guestId;

    @NotNull(message = "Points to redeem is mandatory")
    @Min(value = 1, message = "Must redeem at least 1 point")
    private Integer points;

    private String description;
}
