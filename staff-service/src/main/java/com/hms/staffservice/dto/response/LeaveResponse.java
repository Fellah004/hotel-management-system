package com.hms.staffservice.dto.response;

import com.hms.staffservice.entity.LeaveStatus;
import com.hms.staffservice.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {
    private Long id;
    private Long staffId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private Long approvedByStaffId;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
