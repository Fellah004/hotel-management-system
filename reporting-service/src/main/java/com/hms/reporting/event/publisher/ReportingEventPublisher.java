package com.hms.reportingservice.event.publisher;

import com.hms.reportingservice.config.RabbitMQConfig;
import com.hms.reportingservice.entity.DailySummary;
import com.hms.reportingservice.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDailySummaryGenerated(DailySummary summary) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("summaryId", summary.getId());
        payload.put("summaryDate", summary.getSummaryDate().toString());
        payload.put("occupancyPercentage", summary.getOccupancyPercentage());
        payload.put("totalRevenue", summary.getTotalRevenue());
        payload.put("totalExpenses", summary.getTotalExpenses());

        EventEnvelope envelope = EventEnvelope.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DailySummaryGenerated")
                .sourceService("reporting-service")
                .correlationId(MDC.get("correlationId") != null ? MDC.get("correlationId") : UUID.randomUUID().toString())
                .occurredAt(LocalDateTime.now())
                .payload(payload)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.HOTEL_EXCHANGE, "hotel.reporting.daily-summary", envelope);
        log.info("Published DailySummaryGenerated event for date: {}", summary.getSummaryDate());
    }
}
