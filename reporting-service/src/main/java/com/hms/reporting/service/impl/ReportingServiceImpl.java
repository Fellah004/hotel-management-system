package com.hms.reportingservice.service.impl;

import com.hms.reportingservice.dto.response.*;
import com.hms.reportingservice.entity.DailySummary;
import com.hms.reportingservice.entity.OccupancySnapshot;
import com.hms.reportingservice.entity.RevenueRecord;
import com.hms.reportingservice.entity.StaffPerformanceRecord;
import com.hms.reportingservice.event.publisher.ReportingEventPublisher;
import com.hms.reportingservice.exception.ResourceNotFoundException;
import com.hms.reportingservice.repository.DailySummaryRepository;
import com.hms.reportingservice.repository.OccupancySnapshotRepository;
import com.hms.reportingservice.repository.RevenueRecordRepository;
import com.hms.reportingservice.repository.StaffPerformanceRecordRepository;
import com.hms.reportingservice.service.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingServiceImpl implements ReportingService {

    private final DailySummaryRepository dailySummaryRepository;
    private final RevenueRecordRepository revenueRecordRepository;
    private final OccupancySnapshotRepository occupancySnapshotRepository;
    private final StaffPerformanceRecordRepository staffPerformanceRecordRepository;
    private final ReportingEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public OccupancyReportResponse getOccupancyReport(LocalDate startDate, LocalDate endDate) {
        List<OccupancySnapshot> snapshots = occupancySnapshotRepository.findBySnapshotDateBetween(startDate, endDate);
        int totalRooms = snapshots.isEmpty() ? 100 : snapshots.get(0).getTotalRooms();
        int occupied = (int) snapshots.stream().mapToInt(OccupancySnapshot::getOccupiedRooms).average().orElse(0.0);
        int available = (int) snapshots.stream().mapToInt(OccupancySnapshot::getAvailableRooms).average().orElse(0.0);
        int dirty = (int) snapshots.stream().mapToInt(OccupancySnapshot::getDirtyRooms).average().orElse(0.0);
        int cleaning = (int) snapshots.stream().mapToInt(OccupancySnapshot::getCleaningRooms).average().orElse(0.0);
        int maintenance = (int) snapshots.stream().mapToInt(OccupancySnapshot::getMaintenanceRooms).average().orElse(0.0);
        int outOfService = (int) snapshots.stream().mapToInt(OccupancySnapshot::getOutOfServiceRooms).average().orElse(0.0);

        double avgPercentage = snapshots.stream()
                .map(s -> s.getOccupancyPercentage() != null ? s.getOccupancyPercentage() : BigDecimal.ZERO)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        return OccupancyReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRooms(totalRooms)
                .occupiedRooms(occupied)
                .availableRooms(available)
                .dirtyRooms(dirty)
                .cleaningRooms(cleaning)
                .maintenanceRooms(maintenance)
                .outOfServiceRooms(outOfService)
                .occupancyPercentage(BigDecimal.valueOf(avgPercentage).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<RevenueRecord> records = revenueRecordRepository.findByRecordDateBetween(startDate, endDate);
        BigDecimal roomRev = records.stream()
                .filter(r -> "ROOM".equalsIgnoreCase(r.getSource()))
                .map(RevenueRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal hallRev = records.stream()
                .filter(r -> "HALL".equalsIgnoreCase(r.getSource()))
                .map(RevenueRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal serviceRev = records.stream()
                .filter(r -> "SERVICE".equalsIgnoreCase(r.getSource()))
                .map(RevenueRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal otherRev = records.stream()
                .filter(r -> !"ROOM".equalsIgnoreCase(r.getSource()) && !"HALL".equalsIgnoreCase(r.getSource()) && !"SERVICE".equalsIgnoreCase(r.getSource()))
                .map(RevenueRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRev = roomRev.add(hallRev).add(serviceRev).add(otherRev);

        return RevenueReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .roomRevenue(roomRev)
                .hallRevenue(hallRev)
                .serviceRevenue(serviceRev)
                .otherRevenue(otherRev)
                .totalRevenue(totalRev)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseReportResponse getExpenseReport(LocalDate startDate, LocalDate endDate) {
        return ExpenseReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalExpenses(BigDecimal.ZERO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffPerformanceReportResponse getStaffPerformanceReport(LocalDate startDate, LocalDate endDate) {
        List<StaffPerformanceRecord> records = staffPerformanceRecordRepository.findByRecordDateBetween(startDate, endDate);
        int totalCompleted = records.stream().mapToInt(StaffPerformanceRecord::getTasksCompleted).sum();
        int totalRejected = records.stream().mapToInt(StaffPerformanceRecord::getTasksRejected).sum();

        List<StaffPerformanceReportResponse.StaffPerformanceDetail> details = records.stream()
                .map(r -> StaffPerformanceReportResponse.StaffPerformanceDetail.builder()
                        .staffId(r.getStaffId())
                        .tasksCompleted(r.getTasksCompleted())
                        .tasksRejected(r.getTasksRejected())
                        .attendanceDays(r.getAttendanceDays())
                        .build())
                .collect(Collectors.toList());

        return StaffPerformanceReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalTasksCompleted(totalCompleted)
                .totalTasksRejected(totalRejected)
                .staffDetails(details)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DailySummaryResponse getDailySummary(LocalDate date) {
        DailySummary summary = dailySummaryRepository.findBySummaryDate(date)
                .orElseThrow(() -> new ResourceNotFoundException("Daily summary not found for date: " + date));
        return mapToResponse(summary);
    }

    @Override
    @Transactional
    public DailySummaryResponse generateDailySummary(LocalDate date) {
        log.info("Generating daily summary for date: {}", date);
        DailySummary summary = dailySummaryRepository.findBySummaryDate(date)
                .orElse(DailySummary.builder()
                        .summaryDate(date)
                        .totalArrivals(0)
                        .totalDepartures(0)
                        .totalReservations(0)
                        .totalCancellations(0)
                        .totalNoShows(0)
                        .occupiedRooms(0)
                        .occupancyPercentage(BigDecimal.ZERO)
                        .totalRevenue(BigDecimal.ZERO)
                        .totalExpenses(BigDecimal.ZERO)
                        .pendingMaintenanceCount(0)
                        .pendingComplaintsCount(0)
                        .lowStockCount(0)
                        .pendingServiceRequestsCount(0)
                        .pendingHousekeepingTasksCount(0)
                        .generatedAt(LocalDateTime.now())
                        .build());

        // Calculate revenue from records
        List<RevenueRecord> records = revenueRecordRepository.findByRecordDateBetween(date, date);
        BigDecimal totalRev = records.stream().map(RevenueRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalRevenue(totalRev);
        summary.setGeneratedAt(LocalDateTime.now());

        summary = dailySummaryRepository.save(summary);
        eventPublisher.publishDailySummaryGenerated(summary);

        return mapToResponse(summary);
    }

    private DailySummaryResponse mapToResponse(DailySummary summary) {
        return DailySummaryResponse.builder()
                .id(summary.getId())
                .summaryDate(summary.getSummaryDate())
                .arrivals(summary.getTotalArrivals() != null ? summary.getTotalArrivals() : 0)
                .departures(summary.getTotalDepartures() != null ? summary.getTotalDepartures() : 0)
                .totalReservations(summary.getTotalReservations() != null ? summary.getTotalReservations() : 0)
                .cancellations(summary.getTotalCancellations() != null ? summary.getTotalCancellations() : 0)
                .noShows(summary.getTotalNoShows() != null ? summary.getTotalNoShows() : 0)
                .occupiedRooms(summary.getOccupiedRooms() != null ? summary.getOccupiedRooms() : 0)
                .occupancyPercentage(summary.getOccupancyPercentage() != null ? summary.getOccupancyPercentage().doubleValue() : 0.0)
                .roomRevenue(BigDecimal.ZERO)
                .serviceRevenue(BigDecimal.ZERO)
                .totalRevenue(summary.getTotalRevenue() != null ? summary.getTotalRevenue() : BigDecimal.ZERO)
                .totalExpenses(summary.getTotalExpenses() != null ? summary.getTotalExpenses() : BigDecimal.ZERO)
                .pendingMaintenance(summary.getPendingMaintenanceCount() != null ? summary.getPendingMaintenanceCount() : 0)
                .complaintsLogged(summary.getPendingComplaintsCount() != null ? summary.getPendingComplaintsCount() : 0)
                .lowStockItems(summary.getLowStockCount() != null ? summary.getLowStockCount() : 0)
                .pendingServiceRequests(summary.getPendingServiceRequestsCount() != null ? summary.getPendingServiceRequestsCount() : 0)
                .pendingHousekeepingTasks(summary.getPendingHousekeepingTasksCount() != null ? summary.getPendingHousekeepingTasksCount() : 0)
                .generatedAt(summary.getGeneratedAt())
                .build();
    }
}
