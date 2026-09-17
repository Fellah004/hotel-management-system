package com.hms.staffservice.dto.request;

import com.hms.staffservice.entity.StaffRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffRequest {
    private String fullName;
    private String address;
    private String nic;

    @Positive(message = "Salary must be positive")
    private BigDecimal salary;

    private Integer age;
    private String occupation;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private StaffRole role;
    private Boolean active;
}
