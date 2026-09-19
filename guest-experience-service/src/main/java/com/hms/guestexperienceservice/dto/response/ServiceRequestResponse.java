package com.hms.guestexperienceservice.dto.response;

import com.hms.guestexperienceservice.entity.RequestStatus;
import com.hms.guestexperienceservice.entity.RequestType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestResponse {
    private Long id;
    private String requestCode;
    private Long reservationId;
    private Long guestId;
    private Long roomId;
    private RequestType requestType;
    private String description;
    private Integer quantity;
    private String priority;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
