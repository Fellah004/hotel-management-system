package com.hms.reportingservice.service.impl;

import com.hms.reportingservice.client.OperationsClient;
import com.hms.reportingservice.client.ReservationClient;
import com.hms.reportingservice.client.RoomClient;
import com.hms.reportingservice.client.dto.*;
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
import java.util.*;
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

    private final RoomClient roomClient;
    private final ReservationClient reservationClient;
    private final OperationsClient operationsClient;

    @Override
    @Transactional
    public OccupancyReportResponse getOccupancyReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating occupancy report from {} to {}", startDate, endDate);

        List<RoomDto> rooms = new ArrayList<>();
        try {
            rooms = roomClient.getAllRooms(true);
        } catch (Exception e) {
            log.warn("Could not fetch live room list from room-service: {}", e.getMessage());
        }

        int totalRooms = !rooms.isEmpty() ? rooms.size() : 10;
        int occupied = 0;
        int available = 0;
        int dirty = 0;
        int cleaning = 0;
        int maintenance = 0;
        int outOfService = 0;

        for (RoomDto r : rooms) {
            if (!r.isActive()) {
                outOfService++;
                continue;
            }
            String status = r.getStatus() != null ? r.getStatus().toUpperCase() : "AVAILABLE";
            switch (status) {
                case "OCCUPIED":
                    occupied++;
                    break;
                case "DIRTY":
                    dirty++;
                    break;
                case "CLEANING":
                    cleaning++;
                    break;
                case "MAINTENANCE":
                    maintenance++;
                    break;
                case "OUT_OF_SERVICE":
                    outOfService++;
                    break;
                case "AVAILABLE":
                default:
                    available++;
                    break;
            }
        }

        BigDecimal occupancyPercentage = totalRooms > 0
                ? BigDecimal.valueOf((occupied * 100.0) / totalRooms).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Save snapshot for today if within date range
        LocalDate today = LocalDate.now();
        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            OccupancySnapshot snapshot = occupancySnapshotRepository.findBySnapshotDate(today)
                    .orElse(OccupancySnapshot.builder().snapshotDate(today).build());
            snapshot.setTotalRooms(totalRooms);
            snapshot.setOccupiedRooms(occupied);
            snapshot.setAvailableRooms(available);
            snapshot.setDirtyRooms(dirty);
            snapshot.setCleaningRooms(cleaning);
            snapshot.setMaintenanceRooms(maintenance);
            snapshot.setOutOfServiceRooms(outOfService);
            snapshot.setOccupancyPercentage(occupancyPercentage);
            occupancySnapshotRepository.save(snapshot);
        }

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
                .occupancyPercentage(occupancyPercentage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating revenue report from {} to {}", startDate, endDate);

        BigDecimal roomRev = BigDecimal.ZERO;
        BigDecimal hallRev = BigDecimal.ZERO;
        BigDecimal serviceRev = BigDecimal.ZERO;
        BigDecimal otherRev = BigDecimal.ZERO;

        // 1. Fetch live reservations across the period
        try {
            List<ReservationDto> reservations = reservationClient.getAllReservations();
            if (reservations != null) {
                for (ReservationDto res : reservations) {
                    if ("CANCELLED".equalsIgnoreCase(res.getStatus())) {
                        continue;
                    }
                    LocalDate checkIn = res.getCheckInDateTime() != null ? res.getCheckInDateTime().toLocalDate() : null;
                    LocalDate checkOut = res.getCheckOutDateTime() != null ? res.getCheckOutDateTime().toLocalDate() : null;

                    if (checkIn != null && checkOut != null) {
                        // Check if stay overlaps with [startDate, endDate]
                        if (!checkIn.isAfter(endDate) && !checkOut.isBefore(startDate)) {
                            BigDecimal amount = res.getQuotedAmount() != null ? res.getQuotedAmount() : BigDecimal.ZERO;
                            if ("HALL".equalsIgnoreCase(res.getResourceType())) {
                                hallRev = hallRev.add(amount);
                            } else {
                                roomRev = roomRev.add(amount);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve live reservations for revenue report: {}", e.getMessage());
        }

        // 2. Aggregate local revenue records (from payments/events)
        List<RevenueRecord> records = revenueRecordRepository.findByRecordDateBetween(startDate, endDate);
        for (RevenueRecord r : records) {
            if ("SERVICE".equalsIgnoreCase(r.getSource())) {
                serviceRev = serviceRev.add(r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO);
            } else if (!"ROOM".equalsIgnoreCase(r.getSource()) && !"HALL".equalsIgnoreCase(r.getSource())) {
                otherRev = otherRev.add(r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO);
            }
        }

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
        log.info("Generating expense report from {} to {}", startDate, endDate);

        BigDecimal totalExpenses = BigDecimal.ZERO;
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        try {
            List<ExpenseDto> expenses = operationsClient.getAllExpenses();
            if (expenses != null) {
                for (ExpenseDto exp : expenses) {
                    LocalDate expDate = exp.getExpenseDate() != null ? exp.getExpenseDate() : LocalDate.now();
                    if (!expDate.isBefore(startDate) && !expDate.isAfter(endDate)) {
                        BigDecimal amount = exp.getAmount() != null ? exp.getAmount() : BigDecimal.ZERO;
                        totalExpenses = totalExpenses.add(amount);
                        String category = exp.getCategory() != null ? exp.getCategory() : "GENERAL";
                        expensesByCategory.put(category, expensesByCategory.getOrDefault(category, BigDecimal.ZERO).add(amount));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve expenses from operations-service: {}", e.getMessage());
        }

        return ExpenseReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalExpenses(totalExpenses)
                .expensesByCategory(expensesByCategory)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffPerformanceReportResponse getStaffPerformanceReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating staff performance report from {} to {}", startDate, endDate);

        int totalCompleted = 0;
        int totalRejected = 0;
        Map<Long, StaffPerformanceReportResponse.StaffPerformanceDetail> staffMap = new HashMap<>();

        // 1. Fetch live Housekeeping tasks from operations-service
        try {
            List<HousekeepingTaskDto> tasks = operationsClient.getAllHousekeepingTasks();
            if (tasks != null) {
                for (HousekeepingTaskDto task : tasks) {
                    LocalDate taskDate = task.getCreatedAt() != null ? task.getCreatedAt().toLocalDate() : LocalDate.now();
                    if (!taskDate.isBefore(startDate) && !taskDate.isAfter(endDate)) {
                        Long staffId = task.getAssignedStaffId() != null ? task.getAssignedStaffId() : 0L;
                        StaffPerformanceReportResponse.StaffPerformanceDetail detail = staffMap.computeIfAbsent(staffId, id ->
                                StaffPerformanceReportResponse.StaffPerformanceDetail.builder()
                                        .staffId(id)
                                        .tasksCompleted(0)
                                        .tasksRejected(0)
                                        .attendanceDays(1)
                                        .build());

                        if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
                            totalCompleted++;
                            detail.setTasksCompleted(detail.getTasksCompleted() + 1);
                        } else if ("REJECTED".equalsIgnoreCase(task.getStatus()) || "CANCELLED".equalsIgnoreCase(task.getStatus())) {
                            totalRejected++;
                            detail.setTasksRejected(detail.getTasksRejected() + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch housekeeping tasks from operations-service: {}", e.getMessage());
        }

        // 2. Merge local records if present
        List<StaffPerformanceRecord> records = staffPerformanceRecordRepository.findByRecordDateBetween(startDate, endDate);
        for (StaffPerformanceRecord r : records) {
            totalCompleted += r.getTasksCompleted();
            totalRejected += r.getTasksRejected();
            StaffPerformanceReportResponse.StaffPerformanceDetail detail = staffMap.computeIfAbsent(r.getStaffId(), id ->
                    StaffPerformanceReportResponse.StaffPerformanceDetail.builder()
                            .staffId(id)
                            .tasksCompleted(0)
                            .tasksRejected(0)
                            .attendanceDays(r.getAttendanceDays())
                            .build());
            detail.setTasksCompleted(detail.getTasksCompleted() + r.getTasksCompleted());
            detail.setTasksRejected(detail.getTasksRejected() + r.getTasksRejected());
        }

        return StaffPerformanceReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalTasksCompleted(totalCompleted)
                .totalTasksRejected(totalRejected)
                .staffDetails(new ArrayList<>(staffMap.values()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DailySummaryResponse getDailySummary(LocalDate date) {
        DailySummary summary = dailySummaryRepository.findBySummaryDate(date)
                .orElseGet(() -> generateSummaryEntity(date));
        return mapToResponse(summary);
    }

    @Override
    @Transactional
    public DailySummaryResponse generateDailySummary(LocalDate date) {
        log.info("Generating daily summary for date: {}", date);
        DailySummary summary = generateSummaryEntity(date);
        dailySummaryRepository.findBySummaryDate(date).ifPresent(existing -> summary.setId(existing.getId()));
        DailySummary savedSummary = dailySummaryRepository.save(summary);
        eventPublisher.publishDailySummaryGenerated(savedSummary);
        return mapToResponse(savedSummary);
    }

    private DailySummary generateSummaryEntity(LocalDate date) {
        int arrivals = 0;
        int departures = 0;
        int totalReservations = 0;
        int cancellations = 0;
        int noShows = 0;
        BigDecimal roomRev = BigDecimal.ZERO;

        try {
            List<ReservationDto> reservations = reservationClient.getAllReservations();
            if (reservations != null) {
                for (ReservationDto r : reservations) {
                    LocalDate in = r.getCheckInDateTime() != null ? r.getCheckInDateTime().toLocalDate() : null;
                    LocalDate out = r.getCheckOutDateTime() != null ? r.getCheckOutDateTime().toLocalDate() : null;

                    if (in != null && in.equals(date)) {
                        arrivals++;
                    }
                    if (out != null && out.equals(date)) {
                        departures++;
                    }
                    if ("CANCELLED".equalsIgnoreCase(r.getStatus())) {
                        cancellations++;
                    } else if ("NO_SHOW".equalsIgnoreCase(r.getStatus())) {
                        noShows++;
                    } else if (in != null && out != null && !date.isBefore(in) && !date.isAfter(out)) {
                        totalReservations++;
                        if (r.getQuotedAmount() != null) {
                            int nights = r.getNights() != null && r.getNights() > 0 ? r.getNights() : 1;
                            BigDecimal dailyRate = r.getQuotedAmount().divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
                            roomRev = roomRev.add(dailyRate);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch reservations for daily summary: {}", e.getMessage());
        }

        // Room occupancy
        int totalRooms = 10;
        int occupied = 0;
        try {
            List<RoomDto> rooms = roomClient.getAllRooms(false);
            if (rooms != null && !rooms.isEmpty()) {
                totalRooms = rooms.size();
                occupied = (int) rooms.stream().filter(r -> "OCCUPIED".equalsIgnoreCase(r.getStatus())).count();
            }
        } catch (Exception e) {
            log.warn("Could not fetch rooms for daily summary: {}", e.getMessage());
        }

        BigDecimal occupancyPercent = totalRooms > 0
                ? BigDecimal.valueOf((occupied * 100.0) / totalRooms).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Expenses
        BigDecimal totalExpenses = BigDecimal.ZERO;
        try {
            List<ExpenseDto> expenses = operationsClient.getAllExpenses();
            if (expenses != null) {
                totalExpenses = expenses.stream()
                        .filter(e -> date.equals(e.getExpenseDate()))
                        .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception e) {
            log.warn("Could not fetch expenses for daily summary: {}", e.getMessage());
        }

        // Pending maintenance & housekeeping tasks
        int pendingMaintenance = 0;
        try {
            List<MaintenanceIssueDto> issues = operationsClient.getAllMaintenanceIssues();
            if (issues != null) {
                pendingMaintenance = (int) issues.stream()
                        .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()))
                        .count();
            }
        } catch (Exception e) {
            log.warn("Could not fetch maintenance issues for daily summary: {}", e.getMessage());
        }

        int pendingHousekeeping = 0;
        try {
            List<HousekeepingTaskDto> tasks = operationsClient.getAllHousekeepingTasks();
            if (tasks != null) {
                pendingHousekeeping = (int) tasks.stream()
                        .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()))
                        .count();
            }
        } catch (Exception e) {
            log.warn("Could not fetch housekeeping tasks for daily summary: {}", e.getMessage());
        }

        return DailySummary.builder()
                .summaryDate(date)
                .totalArrivals(arrivals)
                .totalDepartures(departures)
                .totalReservations(totalReservations)
                .totalCancellations(cancellations)
                .totalNoShows(noShows)
                .occupiedRooms(occupied)
                .occupancyPercentage(occupancyPercent)
                .totalRevenue(roomRev)
                .totalExpenses(totalExpenses)
                .pendingMaintenanceCount(pendingMaintenance)
                .pendingComplaintsCount(0)
                .lowStockCount(0)
                .pendingServiceRequestsCount(0)
                .pendingHousekeepingTasksCount(pendingHousekeeping)
                .generatedAt(LocalDateTime.now())
                .build();
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
                .roomRevenue(summary.getTotalRevenue() != null ? summary.getTotalRevenue() : BigDecimal.ZERO)
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
