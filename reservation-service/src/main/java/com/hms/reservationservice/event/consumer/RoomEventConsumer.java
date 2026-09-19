package com.hms.reservationservice.event.consumer;

import com.hms.reservationservice.entity.Waitlist;
import com.hms.reservationservice.entity.WaitlistStatus;
import com.hms.reservationservice.event.EventEnvelope;
import com.hms.reservationservice.event.RabbitMQConfig;
import com.hms.reservationservice.event.publisher.ReservationEventPublisher;
import com.hms.reservationservice.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventConsumer {

    private final WaitlistRepository waitlistRepository;
    private final ReservationEventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_ROOM_QUEUE)
    @Transactional
    public void handleRoomAvailableEvent(EventEnvelope<Map<String, Object>> event) {
        log.info("Received room event: {} (eventId: {})", event.getEventType(), event.getEventId());

        Map<String, Object> payload = event.getPayload();
        if (payload == null || !payload.containsKey("roomId")) {
            return;
        }

        Long roomId = Long.valueOf(payload.get("roomId").toString());

        // Check active waitlists and notify first eligible candidate
        List<Waitlist> activeWaitlists = waitlistRepository.findByStatusOrderByPriorityDescCreatedAtAsc(WaitlistStatus.ACTIVE);
        if (!activeWaitlists.isEmpty()) {
            Waitlist topWaitlist = activeWaitlists.get(0);
            log.info("Found eligible waitlist entry {} for room {}", topWaitlist.getId(), roomId);
            eventPublisher.publishWaitlistAvailabilityDetected(
                    topWaitlist.getId(),
                    topWaitlist.getGuestId(),
                    roomId,
                    topWaitlist.getRoomCategoryId()
            );
        }
    }
}
