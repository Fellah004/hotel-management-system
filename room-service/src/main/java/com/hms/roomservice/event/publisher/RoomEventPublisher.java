package com.hms.roomservice.event.publisher;

import com.hms.roomservice.entity.RoomStatus;
import com.hms.roomservice.event.EventEnvelope;
import com.hms.roomservice.event.RabbitMQConfig;
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
public class RoomEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private void publish(String eventType, String routingKey, Map<String, Object> payload) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService("room-service")
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

    public void publishRoomStatusChanged(Long roomId, String roomNumber, RoomStatus oldStatus, RoomStatus newStatus) {
        Map<String, Object> payload = Map.of(
                "roomId", roomId,
                "roomNumber", roomNumber,
                "oldStatus", oldStatus.name(),
                "newStatus", newStatus.name(),
                "timestamp", LocalDateTime.now().toString()
        );

        if (newStatus == RoomStatus.DIRTY) {
            publish("RoomMarkedDirty", "hms.room.dirty", payload);
        } else if (newStatus == RoomStatus.CLEAN) {
            publish("RoomMarkedClean", "hms.room.clean", payload);
        } else if (newStatus == RoomStatus.AVAILABLE) {
            publish("RoomAvailable", "hms.room.available", payload);
        } else if (newStatus == RoomStatus.MAINTENANCE) {
            publish("RoomMaintenanceStarted", "hms.room.maintenance.started", payload);
        } else {
            publish("RoomStatusChanged", "hms.room.status.changed", payload);
        }
    }
}
