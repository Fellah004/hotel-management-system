package com.hms.guestservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGuestRequest {
    private String firstName;
    private String lastName;

    @Pattern(regexp = "^[+0-9\\- ]{7,20}$", message = "Phone number is invalid")
    private String phone;

    private String company;

    @Email(message = "Email must be valid")
    private String email;

    private String gender;
    private String address;
}
