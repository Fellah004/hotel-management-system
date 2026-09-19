package com.hms.reportingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailySummaryScheduler {

    private final ReportingService reportingService;

    // Run every day at 23:59:00
    @Scheduled(cron = "0 59 23 * * ?")
    public void scheduleDailySummary() {
        log.info("Triggering scheduled automated daily hotel summary generation");
        reportingService.generateDailySummary(LocalDate.now());
    }
}
