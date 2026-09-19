package com.hms.reportingservice.service;

import com.hms.reportingservice.dto.response.*;

import java.time.LocalDate;

public interface ReportingService {
    OccupancyReportResponse getOccupancyReport(LocalDate startDate, LocalDate endDate);
    RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate);
    ExpenseReportResponse getExpenseReport(LocalDate startDate, LocalDate endDate);
    StaffPerformanceReportResponse getStaffPerformanceReport(LocalDate startDate, LocalDate endDate);
    DailySummaryResponse getDailySummary(LocalDate date);
    DailySummaryResponse generateDailySummary(LocalDate date);
}
