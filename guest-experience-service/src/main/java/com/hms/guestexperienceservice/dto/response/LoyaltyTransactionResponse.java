package com.hms.guestexperienceservice.dto.response;

import com.hms.guestexperienceservice.entity.LoyaltyTransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransactionResponse {
    private Long id;
    private Long loyaltyAccountId;
    private Integer points;
    private LoyaltyTransactionType transactionType;
    private String description;
    private LocalDateTime createdAt;
}
