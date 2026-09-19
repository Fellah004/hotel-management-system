package com.hms.reportingservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyReportResponse {
    private LocalDate reportDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalRooms;
    private Integer occupiedRooms;
    private Integer availableRooms;
    private Integer dirtyRooms;
    private Integer cleaningRooms;
    private Integer maintenanceRooms;
    private Integer outOfServiceRooms;
    private BigDecimal occupancyPercentage;
}
