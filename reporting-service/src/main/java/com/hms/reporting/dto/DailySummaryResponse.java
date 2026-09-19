package com.hms.reportingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryResponse {
    private Long id;
    private LocalDate summaryDate;
    private int arrivals;
    private int departures;
    private int totalReservations;
    private int cancellations;
    private int noShows;
    private int occupiedRooms;
    private double occupancyPercentage;
    private BigDecimal roomRevenue;
    private BigDecimal serviceRevenue;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private int pendingMaintenance;
    private int complaintsLogged;
    private int lowStockItems;
    private int pendingServiceRequests;
    private int pendingHousekeepingTasks;
    private LocalDateTime generatedAt;
}
