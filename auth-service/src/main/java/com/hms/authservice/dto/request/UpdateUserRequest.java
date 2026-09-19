package com.hms.authservice.dto.request;

import com.hms.authservice.entity.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Email(message = "Email must be valid")
    private String email;

    private String password;

    private Role role;

    private Boolean active;
}
