package com.hms.operationsservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftHandoverResponse {
    private Long id;
    private Long outgoingStaffId;
    private Long incomingStaffId;
    private Long shiftId;
    private String pendingPayments;
    private String arrivals;
    private String departures;
    private String pendingComplaints;
    private String pendingServiceRequests;
    private String pendingHousekeepingTasks;
    private String pendingMaintenance;
    private String cashInformation;
    private String remarks;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
}
