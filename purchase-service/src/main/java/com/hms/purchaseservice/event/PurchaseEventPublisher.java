package com.hms.purchaseservice.event;

import com.hms.purchaseservice.entity.GoodsReceipt;
import com.hms.purchaseservice.entity.GoodsReceiptItem;
import com.hms.purchaseservice.entity.PurchaseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private void publish(String eventType, String routingKey, Map<String, Object> payload) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService("purchase-service")
                .correlationId(correlationId)
                .occurredAt(LocalDateTime.now())
                .payload(payload)
                .build();

        log.info("Publishing {} event with routing key: {}", eventType, routingKey);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
        } catch (Exception ex) {
            log.error("Failed to publish {} event to RabbitMQ: {}", eventType, ex.getMessage());
        }
    }

    public void publishGoodsReceived(GoodsReceipt receipt) {
        List<Map<String, Object>> itemsPayload = receipt.getItems().stream()
                .filter(item -> item.getAcceptedQuantity() != null && item.getAcceptedQuantity() > 0)
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("itemId", item.getItemId());
                    itemMap.put("acceptedQuantity", item.getAcceptedQuantity());
                    itemMap.put("batchNumber", item.getBatchNumber() != null ? item.getBatchNumber() : "");
                    itemMap.put("expiryDate", item.getExpiryDate() != null ? item.getExpiryDate().toString() : "");
                    return itemMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("receiptId", receipt.getId());
        payload.put("receiptCode", receipt.getReceiptCode());
        payload.put("purchaseOrderId", receipt.getPurchaseOrderId());
        payload.put("receivedDate", receipt.getReceivedDate().toString());
        payload.put("items", itemsPayload);

        publish("GoodsReceived", RabbitMQConfig.GOODS_RECEIVED_ROUTING_KEY, payload);
    }

    public void publishPurchaseOrderApproved(PurchaseOrder order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", order.getId());
        payload.put("orderCode", order.getOrderCode());
        payload.put("supplierId", order.getSupplier().getId());
        payload.put("supplierName", order.getSupplier().getName());
        payload.put("grandTotal", order.getGrandTotal());
        payload.put("orderDate", order.getOrderDate().toString());

        publish("PurchaseOrderApproved", RabbitMQConfig.PO_APPROVED_ROUTING_KEY, payload);
    }
}
