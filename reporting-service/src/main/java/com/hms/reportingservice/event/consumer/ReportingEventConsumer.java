package com.hms.reportingservice.event.consumer;

import com.hms.reportingservice.config.RabbitMQConfig;
import com.hms.reportingservice.entity.RevenueRecord;
import com.hms.reportingservice.entity.StaffPerformanceRecord;
import com.hms.reportingservice.event.EventEnvelope;
import com.hms.reportingservice.repository.RevenueRecordRepository;
import com.hms.reportingservice.repository.StaffPerformanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportingEventConsumer {

    private final RevenueRecordRepository revenueRecordRepository;
    private final StaffPerformanceRecordRepository staffPerformanceRecordRepository;

    @RabbitListener(queues = RabbitMQConfig.REPORTING_QUEUE)
    public void handleEvent(EventEnvelope event) {
        log.info("Reporting service received event: {} [ID: {}]", event.getEventType(), event.getEventId());
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        LocalDate recordDate = event.getOccurredAt() != null ? event.getOccurredAt().toLocalDate() : LocalDate.now();

        switch (event.getEventType()) {
            case "PaymentSucceeded":
                handlePaymentSucceeded(payload, recordDate);
                break;
            case "HousekeepingTaskCompleted":
                handleTaskCompleted(payload, recordDate);
                break;
            case "HousekeepingTaskRejected":
                handleTaskRejected(payload, recordDate);
                break;
            case "StaffShiftStarted":
                handleShiftStarted(payload, recordDate);
                break;
            default:
                log.debug("Event {} ignored by reporting consumer", event.getEventType());
        }
    }

    private void handlePaymentSucceeded(Map<String, Object> payload, LocalDate date) {
        try {
            BigDecimal amount = payload.get("amount") != null ? new BigDecimal(payload.get("amount").toString()) : BigDecimal.ZERO;
            Long reservationId = payload.get("reservationId") != null ? Long.valueOf(payload.get("reservationId").toString()) : null;
            RevenueRecord record = RevenueRecord.builder()
                    .recordDate(date)
                    .source("ROOM")
                    .amount(amount)
                    .referenceId(reservationId)
                    .build();
            revenueRecordRepository.save(record);
            log.info("Saved revenue record for payment: {} on {}", amount, date);
        } catch (Exception e) {
            log.error("Error processing PaymentSucceeded event for reporting", e);
        }
    }

    private void handleTaskCompleted(Map<String, Object> payload, LocalDate date) {
        try {
            Long staffId = payload.get("staffId") != null ? Long.valueOf(payload.get("staffId").toString()) : null;
            if (staffId != null) {
                StaffPerformanceRecord record = staffPerformanceRecordRepository
                        .findByStaffIdAndRecordDate(staffId, date)
                        .orElse(StaffPerformanceRecord.builder()
                                .staffId(staffId)
                                .recordDate(date)
                                .tasksCompleted(0)
                                .tasksRejected(0)
                                .shiftsCompleted(0)
                                .attendanceDays(1)
                                .build());
                record.setTasksCompleted(record.getTasksCompleted() + 1);
                staffPerformanceRecordRepository.save(record);
            }
        } catch (Exception e) {
            log.error("Error processing HousekeepingTaskCompleted event for reporting", e);
        }
    }

    private void handleTaskRejected(Map<String, Object> payload, LocalDate date) {
        try {
            Long staffId = payload.get("staffId") != null ? Long.valueOf(payload.get("staffId").toString()) : null;
            if (staffId != null) {
                StaffPerformanceRecord record = staffPerformanceRecordRepository
                        .findByStaffIdAndRecordDate(staffId, date)
                        .orElse(StaffPerformanceRecord.builder()
                                .staffId(staffId)
                                .recordDate(date)
                                .tasksCompleted(0)
                                .tasksRejected(0)
                                .shiftsCompleted(0)
                                .attendanceDays(1)
                                .build());
                record.setTasksRejected(record.getTasksRejected() + 1);
                staffPerformanceRecordRepository.save(record);
            }
        } catch (Exception e) {
            log.error("Error processing HousekeepingTaskRejected event for reporting", e);
        }
    }

    private void handleShiftStarted(Map<String, Object> payload, LocalDate date) {
        try {
            Long staffId = payload.get("staffId") != null ? Long.valueOf(payload.get("staffId").toString()) : null;
            if (staffId != null) {
                StaffPerformanceRecord record = staffPerformanceRecordRepository
                        .findByStaffIdAndRecordDate(staffId, date)
                        .orElse(StaffPerformanceRecord.builder()
                                .staffId(staffId)
                                .recordDate(date)
                                .tasksCompleted(0)
                                .tasksRejected(0)
                                .shiftsCompleted(0)
                                .attendanceDays(0)
                                .build());
                record.setAttendanceDays(1);
                record.setShiftsCompleted(record.getShiftsCompleted() + 1);
                staffPerformanceRecordRepository.save(record);
            }
        } catch (Exception e) {
            log.error("Error processing StaffShiftStarted event for reporting", e);
        }
    }
}
