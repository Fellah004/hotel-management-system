package com.hms.operationsservice.dto.request;

import com.hms.operationsservice.entity.TaskType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHousekeepingTaskRequest {

    @NotNull(message = "Room ID is mandatory")
    private Long roomId;

    private Long reservationId;
    private Long serviceRequestId;

    @NotNull(message = "Task type is mandatory")
    private TaskType taskType;

    private Long assignedStaffId;

    @Builder.Default
    private String priority = "NORMAL";

    private String remarks;
}
