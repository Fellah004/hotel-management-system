package com.hms.purchaseservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovePurchaseOrderRequest {

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    private String remarks;
}
