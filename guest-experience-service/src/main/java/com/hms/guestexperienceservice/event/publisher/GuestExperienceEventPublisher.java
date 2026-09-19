package com.hms.guestexperienceservice.event.publisher;

import com.hms.guestexperienceservice.entity.Complaint;
import com.hms.guestexperienceservice.entity.ServiceRequest;
import com.hms.guestexperienceservice.event.EventEnvelope;
import com.hms.guestexperienceservice.event.RabbitMQConfig;
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
public class GuestExperienceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private void publish(String eventType, String routingKey, Map<String, Object> payload) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService("guest-experience-service")
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

    public void publishServiceRequestCreated(ServiceRequest request) {
        Map<String, Object> payload = Map.of(
                "serviceRequestId", request.getId(),
                "requestCode", request.getRequestCode(),
                "reservationId", request.getReservationId(),
                "guestId", request.getGuestId(),
                "roomId", request.getRoomId(),
                "requestType", request.getRequestType().name(),
                "description", request.getDescription() != null ? request.getDescription() : "",
                "priority", request.getPriority()
        );
        publish("ServiceRequestCreated", "hms.guestexperience.servicerequest.created", payload);
    }

    public void publishComplaintCreated(Complaint complaint) {
        Map<String, Object> payload = Map.of(
                "complaintId", complaint.getId(),
                "complaintCode", complaint.getComplaintCode(),
                "guestId", complaint.getGuestId(),
                "category", complaint.getCategory(),
                "description", complaint.getDescription(),
                "status", complaint.getStatus().name()
        );
        publish("ComplaintCreated", "hms.guestexperience.complaint.created", payload);
    }

    public void publishComplaintResolved(Complaint complaint) {
        Map<String, Object> payload = Map.of(
                "complaintId", complaint.getId(),
                "complaintCode", complaint.getComplaintCode(),
                "guestId", complaint.getGuestId(),
                "resolutionRemarks", complaint.getResolutionRemarks() != null ? complaint.getResolutionRemarks() : "",
                "resolvedAt", complaint.getResolvedAt().toString()
        );
        publish("ComplaintResolved", "hms.guestexperience.complaint.resolved", payload);
    }
}
