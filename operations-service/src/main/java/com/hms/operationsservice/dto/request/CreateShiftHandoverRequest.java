package com.hms.operationsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShiftHandoverRequest {

    @NotNull(message = "Outgoing staff ID is mandatory")
    private Long outgoingStaffId;

    @NotNull(message = "Incoming staff ID is mandatory")
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
}
