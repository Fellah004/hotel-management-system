package com.hms.operationsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomAssignmentRequest {

    @NotNull(message = "Staff ID is mandatory")
    private Long staffId;

    @NotNull(message = "Start room number is mandatory")
    private Integer startRoomNumber;

    @NotNull(message = "End room number is mandatory")
    private Integer endRoomNumber;

    @NotNull(message = "Effective from date is mandatory")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
