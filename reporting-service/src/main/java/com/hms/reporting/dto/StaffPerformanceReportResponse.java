package com.hms.reportingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPerformanceReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalTasksCompleted;
    private int totalTasksRejected;
    private List<StaffPerformanceDetail> staffDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffPerformanceDetail {
        private Long staffId;
        private int tasksCompleted;
        private int tasksRejected;
        private int attendanceDays;
    }
}
