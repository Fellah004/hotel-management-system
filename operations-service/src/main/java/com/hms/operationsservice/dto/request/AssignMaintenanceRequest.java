package com.hms.operationsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignMaintenanceRequest {

    @NotNull(message = "Staff ID is mandatory")
    private Long staffId;
}
