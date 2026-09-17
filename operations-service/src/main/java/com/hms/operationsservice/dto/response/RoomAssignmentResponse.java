package com.hms.operationsservice.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAssignmentResponse {
    private Long id;
    private Long staffId;
    private Integer startRoomNumber;
    private Integer endRoomNumber;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
