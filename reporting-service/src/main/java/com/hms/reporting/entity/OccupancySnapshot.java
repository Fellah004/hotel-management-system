package com.hms.reportingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "occupancy_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate snapshotDate;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
