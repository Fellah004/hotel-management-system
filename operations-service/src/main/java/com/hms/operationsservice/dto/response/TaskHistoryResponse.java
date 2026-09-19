package com.hms.operationsservice.dto.response;

import com.hms.operationsservice.entity.TaskStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskHistoryResponse {
    private Long id;
    private Long taskId;
    private TaskStatus oldStatus;
    private TaskStatus newStatus;
    private String changedBy;
    private String remarks;
    private LocalDateTime createdAt;
}
