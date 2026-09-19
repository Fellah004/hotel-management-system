package com.hms.guestexperienceservice.dto.response;

import com.hms.guestexperienceservice.entity.ComplaintStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintHistoryResponse {
    private Long id;
    private Long complaintId;
    private ComplaintStatus oldStatus;
    private ComplaintStatus newStatus;
    private String changedBy;
    private String remarks;
    private LocalDateTime createdAt;
}
