package com.hms.authservice.event.publisher;

import com.hms.authservice.event.EventEnvelope;
import com.hms.authservice.event.RabbitMQConfig;
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
public class SecurityEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAccountLocked(Long userId, String username, String email, String reason) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AccountLocked")
                .sourceService("auth-service")
                .correlationId(correlationId)
                .occurredAt(LocalDateTime.now())
                .payload(Map.of(
                        "userId", userId,
                        "username", username,
                        "email", email,
                        "reason", reason,
                        "lockedAt", LocalDateTime.now().toString()
                ))
                .build();

        log.warn("Publishing AccountLocked event for user: {}", username);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "hms.security.account-locked", event);
    }
}
