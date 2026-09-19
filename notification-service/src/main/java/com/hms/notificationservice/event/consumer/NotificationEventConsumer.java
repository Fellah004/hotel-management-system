package com.hms.notificationservice.event.consumer;

import com.hms.notificationservice.config.RabbitMQConfig;
import com.hms.notificationservice.entity.Notification;
import com.hms.notificationservice.event.EventEnvelope;
import com.hms.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleEvent(EventEnvelope event) {
        log.info("Notification Service received event: {} [ID: {}]", event.getEventType(), event.getEventId());
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        Notification notification = null;

        switch (event.getEventType()) {
            case "ReservationConfirmed":
                notification = Notification.builder()
                        .recipient(getStr(payload, "guestEmail", "guest@hotel.com"))
                        .recipientRole("GUEST")
                        .title("Reservation Confirmed")
                        .message("Your reservation #" + getStr(payload, "reservationCode", "") + " has been confirmed!")
                        .channel("EMAIL")
                        .status("UNREAD")
                        .referenceType("RESERVATION")
                        .referenceId(getLong(payload, "reservationId"))
                        .build();
                break;

            case "ReservationCancelled":
                notification = Notification.builder()
                        .recipient(getStr(payload, "guestEmail", "guest@hotel.com"))
                        .recipientRole("GUEST")
                        .title("Reservation Cancelled")
                        .message("Your reservation #" + getStr(payload, "reservationCode", "") + " has been cancelled.")
                        .channel("EMAIL")
                        .status("UNREAD")
                        .referenceType("RESERVATION")
                        .referenceId(getLong(payload, "reservationId"))
                        .build();
                break;

            case "PaymentSucceeded":
                notification = Notification.builder()
                        .recipient("finance@hotel.com")
                        .recipientRole("MANAGER")
                        .title("Payment Received")
                        .message("Payment of $" + getStr(payload, "amount", "0.00") + " received for reservation #" + getStr(payload, "reservationId", ""))
                        .channel("IN_APP")
                        .status("UNREAD")
                        .referenceType("PAYMENT")
                        .referenceId(getLong(payload, "paymentId"))
                        .build();
                break;

            case "PaymentFailed":
                notification = Notification.builder()
                        .recipient("frontdesk@hotel.com")
                        .recipientRole("RECEPTIONIST")
                        .title("Payment Failed")
                        .message("Payment failed for reservation #" + getStr(payload, "reservationId", "") + ". Reason: " + getStr(payload, "reason", "Declined"))
                        .channel("IN_APP")
                        .status("UNREAD")
                        .referenceType("PAYMENT")
                        .referenceId(getLong(payload, "paymentId"))
                        .build();
                break;

            case "RefundCompleted":
                notification = Notification.builder()
                        .recipient("manager@hotel.com")
                        .recipientRole("MANAGER")
                        .title("Refund Processed")
                        .message("Refund of $" + getStr(payload, "amount", "0.00") + " processed for payment #" + getStr(payload, "paymentId", ""))
                        .channel("IN_APP")
                        .status("UNREAD")
                        .referenceType("PAYMENT")
                        .referenceId(getLong(payload, "paymentId"))
                        .build();
                break;

            case "LowStockDetected":
                notification = Notification.builder()
                        .recipient("inventory-manager@hotel.com")
                        .recipientRole("MANAGER")
                        .title("Low Stock Alert")
                        .message("Item #" + getStr(payload, "itemCode", "") + " current stock (" + getStr(payload, "currentStock", "0") + ") is below reorder threshold!")
                        .channel("IN_APP")
                        .status("UNREAD")
                        .referenceType("INVENTORY")
                        .referenceId(getLong(payload, "itemId"))
                        .build();
                break;

            case "HousekeepingTaskAssigned":
                notification = Notification.builder()
                        .recipient("staff-" + getStr(payload, "staffId", "unassigned"))
                        .recipientRole("HOUSEKEEPER")
                        .title("New Housekeeping Task")
                        .message("You have been assigned task #" + getStr(payload, "taskCode", "") + " for room " + getStr(payload, "roomId", ""))
                        .channel("IN_APP")
                        .status("UNREAD")
                        .referenceType("TASK")
                        .referenceId(getLong(payload, "taskId"))
                        .build();
                break;

            case "BreakageApproved":
                notification = Notification.builder()
                        .recipient("frontdesk@hotel.com")
                        .recipientRole("RECEPTIONIST")
                        .title("Breakage Charge Added")
                        .message("Damage charge of $" + getStr(payload, "cost", "0.00") + " approved for room " + getStr(payload, "roomId", ""))
                        .channel("IN_APP")
                        .status("UNREAD")
                        .referenceType("BREAKAGE")
                        .referenceId(getLong(payload, "breakageId"))
                        .build();
                break;

            case "ComplaintCreated":
                notification = Notification.builder()
                        .recipient("manager@hotel.com")
                        .recipientRole("MANAGER")
                        .title("New Guest Complaint")
                        .message("Complaint filed: " + getStr(payload, "subject", "Guest complaint") + " for room " + getStr(payload, "roomId", ""))
                        .channel("IN_APP")
                        .status("UNREAD")
                        .referenceType("COMPLAINT")
                        .referenceId(getLong(payload, "complaintId"))
                        .build();
                break;

            case "DailySummaryGenerated":
                notification = Notification.builder()
                        .recipient("owner@hotel.com")
                        .recipientRole("OWNER")
                        .title("Daily Hotel Summary Generated")
                        .message("Daily summary generated for " + getStr(payload, "summaryDate", "") + ". Occupancy: " + getStr(payload, "occupancyPercentage", "0") + "%")
                        .channel("EMAIL")
                        .status("UNREAD")
                        .referenceType("SUMMARY")
                        .referenceId(getLong(payload, "summaryId"))
                        .build();
                break;

            case "AccountLocked":
                notification = Notification.builder()
                        .recipient("admin@hotel.com")
                        .recipientRole("ADMIN")
                        .title("Security Alert: Account Locked")
                        .message("User account '" + getStr(payload, "username", "") + "' has been temporarily locked due to excessive failed logins.")
                        .channel("IN_APP")
                        .status("UNREAD")
                        .referenceType("SECURITY")
                        .referenceId(getLong(payload, "userId"))
                        .build();
                break;

            default:
                log.info("Generic notification captured for event: {}", event.getEventType());
                notification = Notification.builder()
                        .recipient("system@hotel.com")
                        .recipientRole("ADMIN")
                        .title("System Event: " + event.getEventType())
                        .message("Event occurred in " + event.getSourceService())
                        .channel("IN_APP")
                        .status("UNREAD")
                        .build();
        }

        if (notification != null) {
            notificationRepository.save(notification);
            log.info("Saved notification #{} for recipient {} ({})", notification.getId(), notification.getRecipient(), notification.getTitle());
        }
    }

    private String getStr(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val != null) {
            try {
                return Long.parseLong(val.toString());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
