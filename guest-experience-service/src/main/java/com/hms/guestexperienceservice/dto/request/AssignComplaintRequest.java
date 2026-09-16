package com.hms.guestexperienceservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignComplaintRequest {

    @NotNull(message = "Staff ID is mandatory")
    private Long staffId;

    private String changedBy;
}
