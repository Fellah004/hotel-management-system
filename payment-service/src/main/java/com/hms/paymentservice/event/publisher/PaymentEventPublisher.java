package com.hms.paymentservice.event.publisher;

import com.hms.paymentservice.entity.Payment;
import com.hms.paymentservice.entity.Refund;
import com.hms.paymentservice.event.EventEnvelope;
import com.hms.paymentservice.event.RabbitMQConfig;
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
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private void publish(String eventType, String routingKey, Map<String, Object> payload) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService("payment-service")
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

    public void publishPaymentSucceeded(Payment payment) {
        Map<String, Object> payload = Map.of(
                "paymentId", payment.getId(),
                "reservationId", payment.getReservationId(),
                "amount", payment.getAmount(),
                "paymentMethod", payment.getPaymentMethod().name(),
                "idempotencyKey", payment.getIdempotencyKey(),
                "paymentTime", payment.getPaymentTime().toString()
        );
        publish("PaymentSucceeded", "hms.payment.succeeded", payload);
    }

    public void publishPaymentFailed(Long reservationId, String idempotencyKey, String reason) {
        Map<String, Object> payload = Map.of(
                "reservationId", reservationId,
                "idempotencyKey", idempotencyKey,
                "reason", reason != null ? reason : "Payment declined",
                "failedAt", LocalDateTime.now().toString()
        );
        publish("PaymentFailed", "hms.payment.failed", payload);
    }

    public void publishRefundCompleted(Refund refund) {
        Map<String, Object> payload = Map.of(
                "refundId", refund.getId(),
                "paymentId", refund.getPaymentId(),
                "reservationId", refund.getReservationId(),
                "amount", refund.getAmount(),
                "reason", refund.getReason() != null ? refund.getReason() : "",
                "status", refund.getStatus(),
                "refundedAt", refund.getCreatedAt().toString()
        );
        publish("RefundCompleted", "hms.payment.refund.completed", payload);
    }
}
