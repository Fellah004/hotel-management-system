package com.hms.reportingservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousekeepingTaskDto {
    private Long id;
    private Long roomId;
    private Long assignedStaffId;
    private String taskType;
    private String status;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
