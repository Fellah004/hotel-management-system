package com.hms.guestexperienceservice.dto.response;

import com.hms.guestexperienceservice.entity.RequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestHistoryResponse {
    private Long id;
    private Long serviceRequestId;
    private RequestStatus oldStatus;
    private RequestStatus newStatus;
    private String changedBy;
    private String remarks;
    private LocalDateTime createdAt;
}
