package com.hms.reservationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reservationCode;

    @Column(nullable = false)
    private Long guestId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String resourceType = "ROOM"; // ROOM or HALL

    private Long roomId;

    private Long hallId;

    @Column
    private Long roomCategoryId;

    @Column
    private Long hallCategoryId;

    @Column(nullable = false)
    private Integer adults;

    @Column(nullable = false)
    @Builder.Default
    private Integer children = 0;

    @Column(nullable = false)
    private LocalDateTime checkInDateTime;

    @Column(nullable = false)
    private LocalDateTime checkOutDateTime;

    @Column(nullable = false)
    private Integer nights;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quotedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
