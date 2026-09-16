package com.hms.operations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_requests")
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Issue description is required")
    @Size(max = 500, message = "Issue description must not exceed 500 characters")
    private String issueDescription;

    @NotBlank(message = "Priority is required")
    private String priority;

    @NotBlank(message = "Maintenance status is required")
    private String status;

    @NotNull(message = "Reported time is required")
    private LocalDateTime reportedAt;

    public MaintenanceRequest() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
}
