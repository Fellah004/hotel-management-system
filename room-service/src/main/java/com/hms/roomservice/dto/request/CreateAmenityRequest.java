package com.hms.roomservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAmenityRequest {

    @NotBlank(message = "Amenity name is required")
    private String name;

    private String description;
    private String icon;
}
