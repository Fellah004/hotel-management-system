package com.hms.billingservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private String roomNumber;
    private Object category;
    private String resourceType;
    private Integer floor;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private String status;
    private boolean active;
}
