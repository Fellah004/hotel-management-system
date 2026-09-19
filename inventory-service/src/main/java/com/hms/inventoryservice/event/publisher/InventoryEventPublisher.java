package com.hms.inventoryservice.event.publisher;

import com.hms.inventoryservice.event.EventEnvelope;
import com.hms.inventoryservice.event.RabbitMQConfig;
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
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishLowStock(Long itemId, String itemCode, String itemName, int currentStock, int threshold) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("LowStockDetected")
                .sourceService("inventory-service")
                .correlationId(correlationId)
                .occurredAt(LocalDateTime.now())
                .payload(Map.of(
                        "itemId", itemId,
                        "itemCode", itemCode,
                        "itemName", itemName,
                        "currentStock", currentStock,
                        "minimumThreshold", threshold,
                        "detectedAt", LocalDateTime.now().toString()
                ))
                .build();

        log.warn("Publishing LowStockDetected event for item: {} (Stock: {}, Threshold: {})",
                itemName, currentStock, threshold);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "hms.inventory.low-stock", event);
    }
}
