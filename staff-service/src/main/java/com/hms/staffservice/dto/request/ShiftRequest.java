package com.hms.staffservice.dto.request;

import com.hms.staffservice.entity.ShiftType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequest {

    @NotNull(message = "Shift name is required")
    private ShiftType name;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private Set<Long> assignedStaffIds;
    private Boolean active;
}
