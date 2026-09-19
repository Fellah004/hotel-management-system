package com.hms.guestexperienceservice.dto.response;

import com.hms.guestexperienceservice.entity.LoyaltyTier;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccountResponse {
    private Long id;
    private Long guestId;
    private LoyaltyTier tier;
    private Integer pointsBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
