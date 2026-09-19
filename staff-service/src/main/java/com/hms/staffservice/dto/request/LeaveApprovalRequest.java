package com.hms.staffservice.dto.request;

import com.hms.staffservice.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalRequest {

    @NotNull(message = "Approval status is required")
    private LeaveStatus status;

    private String remarks;
}
