package com.hms.operationsservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shift_handovers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftHandover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long outgoingStaffId;

    @Column(nullable = false)
    private Long incomingStaffId;

    private Long shiftId;

    @Column(columnDefinition = "TEXT")
    private String pendingPayments;

    @Column(columnDefinition = "TEXT")
    private String arrivals;

    @Column(columnDefinition = "TEXT")
    private String departures;

    @Column(columnDefinition = "TEXT")
    private String pendingComplaints;

    @Column(columnDefinition = "TEXT")
    private String pendingServiceRequests;

    @Column(columnDefinition = "TEXT")
    private String pendingHousekeepingTasks;

    @Column(columnDefinition = "TEXT")
    private String pendingMaintenance;

    @Column(columnDefinition = "TEXT")
    private String cashInformation;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACKNOWLEDGED

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acknowledgedAt;
}
