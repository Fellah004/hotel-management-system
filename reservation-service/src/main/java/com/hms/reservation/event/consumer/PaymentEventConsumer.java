package com.hms.reservationservice.event.consumer;

import com.hms.reservationservice.entity.Reservation;
import com.hms.reservationservice.entity.ReservationStatus;
import com.hms.reservationservice.event.EventEnvelope;
import com.hms.reservationservice.event.RabbitMQConfig;
import com.hms.reservationservice.event.publisher.ReservationEventPublisher;
import com.hms.reservationservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ReservationRepository reservationRepository;
    private final ReservationEventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_PAYMENT_QUEUE)
    @Transactional
    public void handlePaymentEvent(EventEnvelope<Map<String, Object>> event) {
        log.info("Received payment event: {} (eventId: {})", event.getEventType(), event.getEventId());

        Map<String, Object> payload = event.getPayload();
        if (payload == null || !payload.containsKey("reservationId")) {
            log.warn("Payment event payload missing reservationId: {}", payload);
            return;
        }

        Long reservationId = Long.valueOf(payload.get("reservationId").toString());
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);

        if (reservation == null) {
            log.warn("Reservation not found for payment event: {}", reservationId);
            return;
        }

        if ("PaymentSucceeded".equalsIgnoreCase(event.getEventType())) {
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
                Reservation updated = reservationRepository.save(reservation);
                log.info("Reservation {} confirmed following successful payment", reservation.getReservationCode());
                eventPublisher.publishReservationConfirmed(updated);
            }
        } else if ("PaymentFailed".equalsIgnoreCase(event.getEventType())) {
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                reservation.setStatus(ReservationStatus.CANCELLED);
                Reservation updated = reservationRepository.save(reservation);
                log.warn("Reservation {} cancelled due to payment failure", reservation.getReservationCode());
                eventPublisher.publishReservationCancelled(updated, "Payment processing failed");
            }
        }
    }
}
