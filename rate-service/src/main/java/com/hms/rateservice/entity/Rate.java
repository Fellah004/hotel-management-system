package com.hms.rateservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long categoryId;

    @Column(length = 20)
    private String resourceType; // ROOM or HALL

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal firstNightPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal extensionPrice;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weekendMultiplier = new BigDecimal("1.20"); // 20% surcharge on Fri/Sat

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal holidayMultiplier = new BigDecimal("1.30"); // 30% surcharge on holidays

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal seasonalMultiplier = new BigDecimal("1.15"); // 15% seasonal peak

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal occupancyThreshold = new BigDecimal("80.00"); // >80% occupancy triggers surge

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal occupancyMultiplier = new BigDecimal("1.25"); // 25% surge when occupancy > threshold

    @Builder.Default
    private Integer lastMinuteDays = 2;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal lastMinuteMultiplier = new BigDecimal("1.10"); // 10% surge or discount

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
