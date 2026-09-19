package com.hms.guestservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponse {
    private Long id;
    private Long userId;
    private String memberCode;
    private String firstName;
    private String lastName;
    private String phone;
    private String company;
    private String email;
    private String gender;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
