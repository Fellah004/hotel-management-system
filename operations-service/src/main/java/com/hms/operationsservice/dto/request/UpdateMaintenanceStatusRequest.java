package com.hms.operationsservice.dto.request;

import com.hms.operationsservice.entity.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMaintenanceStatusRequest {

    @NotNull(message = "Status is mandatory")
    private MaintenanceStatus status;

    private BigDecimal cost;
}
