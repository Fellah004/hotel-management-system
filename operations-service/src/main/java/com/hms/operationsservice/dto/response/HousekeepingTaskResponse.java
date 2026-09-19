package com.hms.operationsservice.dto.response;

import com.hms.operationsservice.entity.TaskStatus;
import com.hms.operationsservice.entity.TaskType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousekeepingTaskResponse {
    private Long id;
    private String taskCode;
    private Long roomId;
    private Long reservationId;
    private Long serviceRequestId;
    private TaskType taskType;
    private Long assignedStaffId;
    private String priority;
    private TaskStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime verifiedAt;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
