package com.hms.staffservice.dto.response;

import com.hms.staffservice.entity.StaffRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private Long userId;
    private String employeeCode;
    private String fullName;
    private String address;
    private String nic;
    private BigDecimal salary;
    private Integer age;
    private String occupation;
    private String email;
    private String phone;
    private StaffRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
