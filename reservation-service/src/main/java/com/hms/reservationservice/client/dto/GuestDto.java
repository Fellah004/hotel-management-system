package com.hms.reservationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestDto {
    private Long id;
    private Long userId;
    private String memberCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
