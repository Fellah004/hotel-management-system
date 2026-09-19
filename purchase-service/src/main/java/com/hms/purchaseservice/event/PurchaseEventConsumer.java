package com.hms.purchaseservice.event;

import com.hms.purchaseservice.dto.request.CreatePurchaseRequest;
import com.hms.purchaseservice.dto.request.PurchaseRequestItemRequest;
import com.hms.purchaseservice.entity.PriorityLevel;
import com.hms.purchaseservice.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseEventConsumer {

    private final PurchaseService purchaseService;

    @RabbitListener(queues = RabbitMQConfig.LOW_STOCK_QUEUE)
    public void handleLowStockEvent(EventEnvelope<Map<String, Object>> event) {
        log.info("Received InventoryLowStock event in purchase-service: {}", event);
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        try {
            Object itemIdObj = payload.get("itemId");
            Object itemNameObj = payload.get("itemName");
            Object currentStockObj = payload.get("currentStock");
            Object minThresholdObj = payload.get("minThreshold");

            if (itemIdObj != null) {
                Long itemId = Long.valueOf(itemIdObj.toString());
                String itemName = itemNameObj != null ? itemNameObj.toString() : "Item-" + itemId;
                int minThreshold = minThresholdObj != null ? Integer.parseInt(minThresholdObj.toString()) : 10;
                int currentStock = currentStockObj != null ? Integer.parseInt(currentStockObj.toString()) : 0;
                int recommendedQty = Math.max(minThreshold * 2 - currentStock, minThreshold);

                PurchaseRequestItemRequest itemReq = PurchaseRequestItemRequest.builder()
                        .itemId(itemId)
                        .itemName(itemName)
                        .quantityRequested(recommendedQty)
                        .unitOfMeasure("UNITS")
                        .build();

                CreatePurchaseRequest prRequest = CreatePurchaseRequest.builder()
                        .requesterId(1L) // System / Inventory automated requester
                        .departmentId(1L)
                        .priority(PriorityLevel.HIGH)
                        .reason("Automated Purchase Request triggered by Low-Stock Alert (Current: " + currentStock + ", Threshold: " + minThreshold + ")")
                        .requiredByDate(LocalDate.now().plusDays(3))
                        .items(Collections.singletonList(itemReq))
                        .build();

                purchaseService.createPurchaseRequest(prRequest, 1L, "SYSTEM");
                log.info("Auto-generated draft Purchase Request for low-stock item: {}", itemName);
            }
        } catch (Exception ex) {
            log.error("Failed to process low stock event into purchase request: {}", ex.getMessage());
        }
    }
}
