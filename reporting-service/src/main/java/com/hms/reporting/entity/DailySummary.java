package com.hms.reportingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate summaryDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalArrivals = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalDepartures = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalReservations = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalCancellations = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalNoShows = 0;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalRooms = 100;

    @Column(nullable = false)
    @Builder.Default
    private Integer occupiedRooms = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer availableRooms = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer dirtyRooms = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer cleaningRooms = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer maintenanceRooms = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer outOfServiceRooms = 0;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal occupancyPercentage = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer pendingMaintenanceCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer pendingComplaintsCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer lowStockCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer pendingServiceRequestsCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer pendingHousekeepingTasksCount = 0;

    @Column(nullable = false)
    private LocalDateTime generatedAt;
}
