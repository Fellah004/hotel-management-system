package com.hms.guestexperienceservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarnLoyaltyRequest {

    @NotNull(message = "Guest ID is mandatory")
    private Long guestId;

    @NotNull(message = "Eligible spending amount is mandatory")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
}
