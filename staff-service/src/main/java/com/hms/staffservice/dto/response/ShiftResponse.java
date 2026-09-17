package com.hms.staffservice.dto.response;

import com.hms.staffservice.entity.ShiftType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponse {
    private Long id;
    private ShiftType name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Set<Long> assignedStaffIds;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
