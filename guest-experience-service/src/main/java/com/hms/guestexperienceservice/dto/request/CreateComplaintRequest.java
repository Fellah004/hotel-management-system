package com.hms.guestexperienceservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateComplaintRequest {

    @NotNull(message = "Guest ID is mandatory")
    private Long guestId;

    private Long reservationId;
    private Long roomId;

    @NotBlank(message = "Category is mandatory")
    private String category;

    @NotBlank(message = "Description is mandatory")
    private String description;
}
