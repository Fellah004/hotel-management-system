package com.hms.operationsservice.event.publisher;

import com.hms.operationsservice.entity.BreakageReport;
import com.hms.operationsservice.entity.Expense;
import com.hms.operationsservice.entity.HousekeepingTask;
import com.hms.operationsservice.event.EventEnvelope;
import com.hms.operationsservice.event.RabbitMQConfig;
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
public class OperationsEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private void publish(String eventType, String routingKey, Map<String, Object> payload) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        EventEnvelope<Map<String, Object>> event = EventEnvelope.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .sourceService("operations-service")
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

    public void publishTaskAssigned(HousekeepingTask task) {
        Map<String, Object> payload = Map.of(
                "taskId", task.getId(),
                "taskCode", task.getTaskCode(),
                "roomId", task.getRoomId(),
                "taskType", task.getTaskType().name(),
                "assignedStaffId", task.getAssignedStaffId() != null ? task.getAssignedStaffId() : 0L,
                "status", task.getStatus().name()
        );
        publish("HousekeepingTaskAssigned", "hms.operations.housekeeping.assigned", payload);
    }

    public void publishTaskCompleted(HousekeepingTask task) {
        Map<String, Object> payload = Map.of(
                "taskId", task.getId(),
                "taskCode", task.getTaskCode(),
                "roomId", task.getRoomId(),
                "staffId", task.getAssignedStaffId() != null ? task.getAssignedStaffId() : 0L,
                "reservationId", task.getReservationId() != null ? task.getReservationId() : 0L,
                "serviceRequestId", task.getServiceRequestId() != null ? task.getServiceRequestId() : 0L,
                "taskType", task.getTaskType().name(),
                "completedAt", task.getCompletedAt() != null ? task.getCompletedAt().toString() : LocalDateTime.now().toString()
        );
        publish("HousekeepingTaskCompleted", "hms.operations.housekeeping.completed", payload);
    }

    public void publishTaskRejected(HousekeepingTask task, Long staffId) {
        Map<String, Object> payload = Map.of(
                "taskId", task.getId(),
                "taskCode", task.getTaskCode(),
                "roomId", task.getRoomId(),
                "staffId", staffId != null ? staffId : 0L,
                "remarks", task.getRemarks() != null ? task.getRemarks() : ""
        );
        publish("HousekeepingTaskRejected", "hms.operations.housekeeping.rejected", payload);
    }

    public void publishBreakageApproved(BreakageReport report) {
        Map<String, Object> payload = Map.of(
                "reportId", report.getId(),
                "reportCode", report.getReportCode(),
                "reservationId", report.getReservationId() != null ? report.getReservationId() : 0L,
                "roomId", report.getRoomId() != null ? report.getRoomId() : 0L,
                "description", report.getDescription(),
                "chargeAmount", report.getChargeAmount()
        );
        publish("BreakageApproved", "hms.operations.breakage.approved", payload);
    }

    public void publishExpenseCreated(Expense expense) {
        Map<String, Object> payload = Map.of(
                "expenseId", expense.getId(),
                "category", expense.getCategory(),
                "amount", expense.getAmount(),
                "expenseDate", expense.getExpenseDate().toString()
        );
        publish("ExpenseCreated", "hms.operations.expense.created", payload);
    }
}
