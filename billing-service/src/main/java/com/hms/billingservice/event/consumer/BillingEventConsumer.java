package com.hms.billingservice.event.consumer;

import com.hms.billingservice.dto.request.AddBillItemRequest;
import com.hms.billingservice.entity.Bill;
import com.hms.billingservice.entity.BillItemType;
import com.hms.billingservice.event.EventEnvelope;
import com.hms.billingservice.event.RabbitMQConfig;
import com.hms.billingservice.repository.BillRepository;
import com.hms.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingEventConsumer {

    private final BillingService billingService;
    private final BillRepository billRepository;

    @RabbitListener(queues = RabbitMQConfig.BREAKAGE_QUEUE)
    public void handleBreakageApproved(EventEnvelope<Map<String, Object>> event) {
        log.info("Received BreakageApproved event: {}", event);
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        Object resIdObj = payload.get("reservationId");
        Object amountObj = payload.get("chargeAmount");
        Object descObj = payload.get("description");

        if (resIdObj != null && amountObj != null) {
            Long reservationId = Long.valueOf(resIdObj.toString());
            BigDecimal amount = new BigDecimal(amountObj.toString());
            String desc = descObj != null ? descObj.toString() : "Damage/Breakage charge";

            Optional<Bill> billOpt = billRepository.findByReservationId(reservationId);
            if (billOpt.isPresent()) {
                AddBillItemRequest request = AddBillItemRequest.builder()
                        .description(desc)
                        .itemType(BillItemType.BREAKAGE)
                        .quantity(1)
                        .unitPrice(amount)
                        .build();
                billingService.addItemToBill(billOpt.get().getId(), request);
                log.info("Successfully added breakage charge of {} to bill id {}", amount, billOpt.get().getId());
            } else {
                log.warn("No active bill found for reservationId: {} to apply breakage charge", reservationId);
            }
        }
    }
}
