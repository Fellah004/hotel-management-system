package com.hms.reportingservice.controller;

import com.hms.reportingservice.dto.response.*;
import com.hms.reportingservice.service.ReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private ReportingController reportingController;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportingController).build();
        today = LocalDate.now();
    }

    @Test
    void testGetOccupancyReport_Success() throws Exception {
        OccupancyReportResponse response = OccupancyReportResponse.builder()
                .startDate(today)
                .endDate(today)
                .totalRooms(50)
                .occupiedRooms(35)
                .occupancyPercentage(new BigDecimal("70.00"))
                .build();

        when(reportingService.getOccupancyReport(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/occupancy")
                        .param("startDate", today.toString())
                        .param("endDate", today.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms").value(50))
                .andExpect(jsonPath("$.occupiedRooms").value(35))
                .andExpect(jsonPath("$.occupancyPercentage").value(70.00));
    }

    @Test
    void testGetRevenueReport_Success() throws Exception {
        RevenueReportResponse response = RevenueReportResponse.builder()
                .startDate(today)
                .endDate(today)
                .roomRevenue(new BigDecimal("1500.00"))
                .serviceRevenue(new BigDecimal("200.00"))
                .totalRevenue(new BigDecimal("1700.00"))
                .build();

        when(reportingService.getRevenueReport(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/revenue")
                        .param("startDate", today.toString())
                        .param("endDate", today.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(1700.00));
    }

    @Test
    void testGetDailySummary_Success() throws Exception {
        DailySummaryResponse response = DailySummaryResponse.builder()
                .id(1L)
                .summaryDate(today)
                .arrivals(5)
                .departures(3)
                .totalRevenue(new BigDecimal("2500.00"))
                .generatedAt(LocalDateTime.now())
                .build();

        when(reportingService.getDailySummary(any(LocalDate.class))).thenReturn(response);

        mockMvc.perform(get("/api/reports/daily-summary")
                        .param("date", today.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.arrivals").value(5));
    }

    @Test
    void testGenerateDailySummary_Success() throws Exception {
        DailySummaryResponse response = DailySummaryResponse.builder()
                .id(1L)
                .summaryDate(today)
                .arrivals(10)
                .totalRevenue(new BigDecimal("5000.00"))
                .generatedAt(LocalDateTime.now())
                .build();

        when(reportingService.generateDailySummary(any(LocalDate.class))).thenReturn(response);

        mockMvc.perform(post("/api/reports/daily-summary/generate")
                        .param("date", today.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.arrivals").value(10));
    }
}
