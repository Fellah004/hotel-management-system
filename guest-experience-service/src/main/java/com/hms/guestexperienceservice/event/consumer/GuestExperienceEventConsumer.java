package com.hms.guestexperienceservice.event.consumer;

import com.hms.guestexperienceservice.dto.request.EarnLoyaltyRequest;
import com.hms.guestexperienceservice.entity.RequestStatus;
import com.hms.guestexperienceservice.entity.ServiceRequest;
import com.hms.guestexperienceservice.event.EventEnvelope;
import com.hms.guestexperienceservice.event.RabbitMQConfig;
import com.hms.guestexperienceservice.repository.ServiceRequestRepository;
import com.hms.guestexperienceservice.service.GuestExperienceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GuestExperienceEventConsumer {

    private final ServiceRequestRepository serviceRequestRepository;
    private final GuestExperienceService guestExperienceService;

    @RabbitListener(queues = RabbitMQConfig.HK_TASK_COMPLETED_QUEUE)
    public void handleHousekeepingTaskCompleted(EventEnvelope<Map<String, Object>> event) {
        log.info("Received HousekeepingTaskCompleted event: {}", event);
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        Object serviceReqIdObj = payload.get("serviceRequestId");
        if (serviceReqIdObj != null) {
            Long serviceRequestId = Long.valueOf(serviceReqIdObj.toString());
            Optional<ServiceRequest> reqOpt = serviceRequestRepository.findById(serviceRequestId);
            if (reqOpt.isPresent()) {
                ServiceRequest req = reqOpt.get();
                req.setStatus(RequestStatus.COMPLETED);
                req.setCompletedAt(LocalDateTime.now());
                serviceRequestRepository.save(req);
                log.info("Updated ServiceRequest id: {} to COMPLETED from housekeeping task", serviceRequestId);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCEEDED_QUEUE)
    public void handlePaymentSucceeded(EventEnvelope<Map<String, Object>> event) {
        log.info("Received PaymentSucceeded event in guest-experience-service: {}", event);
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        Object guestIdObj = payload.get("guestId");
        Object amountObj = payload.get("amount");
        if (guestIdObj != null && amountObj != null) {
            Long guestId = Long.valueOf(guestIdObj.toString());
            BigDecimal amount = new BigDecimal(amountObj.toString());
            EarnLoyaltyRequest req = EarnLoyaltyRequest.builder()
                    .guestId(guestId)
                    .amount(amount)
                    .description("Loyalty points earned from payment")
                    .build();
            try {
                guestExperienceService.earnPoints(req);
            } catch (Exception ex) {
                log.error("Could not automatically earn loyalty points: {}", ex.getMessage());
            }
        }
    }
}
