package com.hms.guestexperienceservice.dto.request;

import com.hms.guestexperienceservice.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateComplaintStatusRequest {

    @NotNull(message = "Status is mandatory")
    private ComplaintStatus status;

    private String resolutionRemarks;
    private String changedBy;
}
