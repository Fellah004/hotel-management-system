package com.hms.operationsservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMaintenanceRequest {

    @NotNull(message = "Room ID is mandatory")
    private Long roomId;

    @NotBlank(message = "Description is mandatory")
    private String description;

    private String reportedBy;
}
