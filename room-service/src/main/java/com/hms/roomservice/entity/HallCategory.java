package com.hms.roomservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hall_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // e.g. BANQUET_HALL, CONFERENCE_HALL, BOARD_ROOM, MARRIAGE_HALL, EXHIBITION_HALL

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePerHour;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePerDay;

    @Column(nullable = false)
    private Integer minCapacity;

    @Column(nullable = false)
    private Integer maxCapacity;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
