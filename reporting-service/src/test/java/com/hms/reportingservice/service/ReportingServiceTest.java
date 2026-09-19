package com.hms.reportingservice.service;

import com.hms.reportingservice.dto.response.DailySummaryResponse;
import com.hms.reportingservice.dto.response.OccupancyReportResponse;
import com.hms.reportingservice.dto.response.RevenueReportResponse;
import com.hms.reportingservice.dto.response.StaffPerformanceReportResponse;
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
import com.hms.reportingservice.service.impl.ReportingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private DailySummaryRepository dailySummaryRepository;

    @Mock
    private RevenueRecordRepository revenueRecordRepository;

    @Mock
    private OccupancySnapshotRepository occupancySnapshotRepository;

    @Mock
    private StaffPerformanceRecordRepository staffPerformanceRecordRepository;

    @Mock
    private ReportingEventPublisher eventPublisher;

    @Mock
    private com.hms.reportingservice.client.RoomClient roomClient;

    @Mock
    private com.hms.reportingservice.client.ReservationClient reservationClient;

    @Mock
    private com.hms.reportingservice.client.OperationsClient operationsClient;

    @InjectMocks
    private ReportingServiceImpl reportingService;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
    }

    @Test
    void testGetOccupancyReport_Success() {
        OccupancySnapshot snapshot = OccupancySnapshot.builder()
                .id(1L)
                .snapshotDate(today)
                .totalRooms(50)
                .occupiedRooms(35)
                .availableRooms(10)
                .dirtyRooms(3)
                .cleaningRooms(0)
                .maintenanceRooms(2)
                .outOfServiceRooms(0)
                .occupancyPercentage(new BigDecimal("70.00"))
                .build();

        when(occupancySnapshotRepository.findBySnapshotDateBetween(today, today))
                .thenReturn(List.of(snapshot));

        OccupancyReportResponse response = reportingService.getOccupancyReport(today, today);

        assertNotNull(response);
        assertEquals(50, response.getTotalRooms());
        assertEquals(35, response.getOccupiedRooms());
        assertEquals(0, new BigDecimal("70.00").compareTo(response.getOccupancyPercentage()));
    }

    @Test
    void testGetRevenueReport_Success() {
        RevenueRecord record1 = RevenueRecord.builder()
                .id(1L)
                .recordDate(today)
                .source("ROOM")
                .amount(new BigDecimal("1500.00"))
                .build();
        RevenueRecord record2 = RevenueRecord.builder()
                .id(2L)
                .recordDate(today)
                .source("SERVICE")
                .amount(new BigDecimal("200.00"))
                .build();

        when(revenueRecordRepository.findByRecordDateBetween(today, today))
                .thenReturn(List.of(record1, record2));

        RevenueReportResponse response = reportingService.getRevenueReport(today, today);

        assertNotNull(response);
        assertEquals(0, new BigDecimal("1500.00").compareTo(response.getRoomRevenue()));
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getServiceRevenue()));
        assertEquals(0, new BigDecimal("1700.00").compareTo(response.getTotalRevenue()));
    }

    @Test
    void testGetStaffPerformanceReport_Success() {
        StaffPerformanceRecord record = StaffPerformanceRecord.builder()
                .id(1L)
                .staffId(101L)
                .recordDate(today)
                .tasksCompleted(12)
                .tasksRejected(1)
                .attendanceDays(1)
                .build();

        when(staffPerformanceRecordRepository.findByRecordDateBetween(today, today))
                .thenReturn(List.of(record));

        StaffPerformanceReportResponse response = reportingService.getStaffPerformanceReport(today, today);

        assertNotNull(response);
        assertEquals(12, response.getTotalTasksCompleted());
        assertEquals(1, response.getTotalTasksRejected());
        assertEquals(1, response.getStaffDetails().size());
    }

    @Test
    void testGenerateDailySummary_Success() {
        when(dailySummaryRepository.findBySummaryDate(today)).thenReturn(Optional.empty());
        when(revenueRecordRepository.findByRecordDateBetween(today, today)).thenReturn(Collections.emptyList());
        when(dailySummaryRepository.save(any(DailySummary.class))).thenAnswer(invocation -> {
            DailySummary s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        DailySummaryResponse response = reportingService.generateDailySummary(today);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(today, response.getSummaryDate());
        verify(eventPublisher, times(1)).publishDailySummaryGenerated(any(DailySummary.class));
    }

    @Test
    void testGetDailySummary_CalculatedWhenNotInDb() {
        when(dailySummaryRepository.findBySummaryDate(today)).thenReturn(Optional.empty());

        DailySummaryResponse response = reportingService.getDailySummary(today);
        assertNotNull(response);
        assertEquals(today, response.getSummaryDate());
    }
}
