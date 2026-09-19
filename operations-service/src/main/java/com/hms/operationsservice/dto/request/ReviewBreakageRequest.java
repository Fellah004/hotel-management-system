package com.hms.operationsservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewBreakageRequest {

    @NotBlank(message = "Status must be APPROVED or REJECTED")
    private String status;

    @NotNull(message = "Manager ID is mandatory")
    private Long managerId;

    private String remarks;
}
