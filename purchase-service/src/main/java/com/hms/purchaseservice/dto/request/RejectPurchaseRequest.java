package com.hms.purchaseservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectPurchaseRequest {

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    @NotBlank(message = "Rejection reason is mandatory")
    private String rejectionReason;
}
