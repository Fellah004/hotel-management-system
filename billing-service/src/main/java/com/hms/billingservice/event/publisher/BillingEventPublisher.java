package com.hms.billingservice.event.publisher;

import com.hms.billingservice.entity.Bill;
import com.hms.billingservice.event.EventEnvelope;
import com.hms.billingservice.event.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private void publish(String eventType, String routingKey, Map<String, Object> payload) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService("billing-service")
                .correlationId(correlationId)
                .occurredAt(LocalDateTime.now())
                .payload(payload)
                .build();

        log.info("Publishing {} event with routing key: {}", eventType, routingKey);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} event to RabbitMQ (Broker might be offline): {}", eventType, ex.getMessage());
        }
    }

    public void publishBillFinalized(Bill bill) {
        Map<String, Object> payload = Map.of(
                "billId", bill.getId(),
                "billingNumber", bill.getBillingNumber(),
                "reservationId", bill.getReservationId(),
                "guestId", bill.getGuestId(),
                "totalAmount", bill.getTotalAmount(),
                "paidAmount", bill.getPaidAmount(),
                "outstandingAmount", bill.getOutstandingAmount(),
                "finalizedAt", bill.getFinalizedAt().toString()
        );
        publish("BillFinalized", "hms.billing.finalized", payload);
    }
}
