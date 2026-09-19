package com.hms.purchaseservice.dto.request;

import com.hms.purchaseservice.entity.PriorityLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseRequest {

    private Long departmentId;

    @NotNull(message = "Requester ID is required")
    private Long requesterId;

    @NotNull(message = "Reason is required")
    private String reason;

    private PriorityLevel priority;

    private LocalDate requiredByDate;

    @NotEmpty(message = "Line items cannot be empty")
    @Valid
    private List<PurchaseRequestItemRequest> items;
}
