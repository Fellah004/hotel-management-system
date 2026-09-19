package com.hms.guestexperienceservice.dto.response;

import com.hms.guestexperienceservice.entity.ComplaintStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {
    private Long id;
    private String complaintCode;
    private Long guestId;
    private Long reservationId;
    private Long roomId;
    private String category;
    private String description;
    private ComplaintStatus status;
    private Long assignedStaffId;
    private String resolutionRemarks;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
