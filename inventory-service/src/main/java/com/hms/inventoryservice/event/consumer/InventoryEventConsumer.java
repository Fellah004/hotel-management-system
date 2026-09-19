package com.hms.inventoryservice.event.consumer;

import com.hms.inventoryservice.dto.request.StockMovementRequest;
import com.hms.inventoryservice.event.EventEnvelope;
import com.hms.inventoryservice.event.RabbitMQConfig;
import com.hms.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final InventoryService inventoryService;

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    public void handleInventoryEvent(EventEnvelope<Map<String, Object>> event) {
        log.info("Received event in inventory queue: {} from {}", event.getEventType(), event.getSourceService());

        if ("GoodsReceived".equalsIgnoreCase(event.getEventType())) {
            handleGoodsReceived(event.getPayload());
        }
    }

    private void handleGoodsReceived(Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("items")) {
            log.warn("GoodsReceived event payload missing items: {}", payload);
            return;
        }

        String receiptCode = (String) payload.getOrDefault("receiptCode", "UNKNOWN");
        Object itemsObj = payload.get("items");

        if (itemsObj instanceof List<?> itemsList) {
            for (Object obj : itemsList) {
                if (obj instanceof Map<?, ?> itemMap) {
                    try {
                        Long itemId = Long.valueOf(itemMap.get("itemId").toString());
                        Integer acceptedQty = Integer.valueOf(itemMap.get("acceptedQuantity").toString());

                        if (acceptedQty != null && acceptedQty > 0) {
                            StockMovementRequest movementRequest = StockMovementRequest.builder()
                                    .quantity(acceptedQty)
                                    .reason("Goods Receipt from Purchase Order")
                                    .referenceCode(receiptCode)
                                    .staffId(1L)
                                    .build();

                            inventoryService.stockIn(itemId, movementRequest);
                            log.info("Automatically stocked in {} units of item {} for GRN {}", acceptedQty, itemId, receiptCode);
                        }
                    } catch (Exception e) {
                        log.error("Failed to process GRN item for inventory replenishment: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }
}
