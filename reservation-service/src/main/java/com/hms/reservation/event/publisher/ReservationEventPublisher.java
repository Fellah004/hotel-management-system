package com.hms.reservationservice.event.publisher;

import com.hms.reservationservice.entity.Reservation;
import com.hms.reservationservice.event.EventEnvelope;
import com.hms.reservationservice.event.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private void publish(String eventType, String routingKey, Map<String, Object> payload) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService("reservation-service")
                .correlationId(correlationId)
                .occurredAt(LocalDateTime.now())
                .payload(payload)
                .build();

        log.info("Publishing {} event with routing key: {}", eventType, routingKey);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
    }

    public void publishReservationCreated(Reservation res) {
        Map<String, Object> payload = buildPayload(res);
        publish("ReservationCreated", "hms.reservation.created", payload);
    }

    public void publishReservationConfirmed(Reservation res) {
        Map<String, Object> payload = buildPayload(res);
        publish("ReservationConfirmed", "hms.reservation.confirmed", payload);
    }

    public void publishReservationCancelled(Reservation res, String reason) {
        Map<String, Object> payload = buildPayload(res);
        payload.put("cancellationReason", reason);
        publish("ReservationCancelled", "hms.reservation.cancelled", payload);
    }

    public void publishReservationCheckedIn(Reservation res) {
        Map<String, Object> payload = buildPayload(res);
        publish("ReservationCheckedIn", "hms.reservation.checked-in", payload);
    }

    public void publishReservationCheckedOut(Reservation res) {
        Map<String, Object> payload = buildPayload(res);
        publish("ReservationCheckedOut", "hms.reservation.checked-out", payload);
    }

    public void publishReservationNoShow(Reservation res) {
        Map<String, Object> payload = buildPayload(res);
        publish("ReservationNoShow", "hms.reservation.no-show", payload);
    }

    public void publishWaitlistAvailabilityDetected(Long waitlistId, Long guestId, Long roomId, Long categoryId) {
        Map<String, Object> payload = Map.of(
                "waitlistId", waitlistId,
                "guestId", guestId,
                "roomId", roomId,
                "roomCategoryId", categoryId,
                "detectedAt", LocalDateTime.now().toString()
        );
        publish("WaitlistAvailabilityDetected", "hms.reservation.waitlist.available", payload);
    }

    private Map<String, Object> buildPayload(Reservation res) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", res.getId());
        payload.put("reservationCode", res.getReservationCode());
        payload.put("guestId", res.getGuestId());
        payload.put("resourceType", res.getResourceType());
        if (res.getRoomId() != null) payload.put("roomId", res.getRoomId());
        if (res.getHallId() != null) payload.put("hallId", res.getHallId());
        payload.put("roomCategoryId", res.getRoomCategoryId());
        payload.put("adults", res.getAdults());
        payload.put("children", res.getChildren());
        payload.put("checkInDateTime", res.getCheckInDateTime().toString());
        payload.put("checkOutDateTime", res.getCheckOutDateTime().toString());
        payload.put("nights", res.getNights());
        payload.put("quotedAmount", res.getQuotedAmount());
        payload.put("status", res.getStatus().name());
        return payload;
    }
}
