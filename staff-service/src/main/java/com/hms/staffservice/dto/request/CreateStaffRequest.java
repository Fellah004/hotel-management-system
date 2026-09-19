package com.hms.staffservice.dto.request;

import com.hms.staffservice.entity.StaffRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStaffRequest {

    private Long userId;

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String address;

    @NotBlank(message = "NIC is required")
    private String nic;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be positive")
    private BigDecimal salary;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Staff must be at least 18 years old")
    @Max(value = 100, message = "Age must be valid")
    private Integer age;

    @NotBlank(message = "Occupation is required")
    private String occupation;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Role is required")
    private StaffRole role;
}
