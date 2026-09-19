package com.hms.operationsservice.event.consumer;

import com.hms.operationsservice.dto.request.CreateHousekeepingTaskRequest;
import com.hms.operationsservice.entity.TaskType;
import com.hms.operationsservice.event.EventEnvelope;
import com.hms.operationsservice.event.RabbitMQConfig;
import com.hms.operationsservice.service.OperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationsEventConsumer {

    private final OperationsService operationsService;

    @RabbitListener(queues = RabbitMQConfig.CHECKOUT_QUEUE)
    public void handleReservationCheckedOut(EventEnvelope<Map<String, Object>> event) {
        log.info("Received ReservationCheckedOut event in operations-service: {}", event);
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        Object roomIdObj = payload.get("roomId");
        Object reservationIdObj = payload.get("reservationId");

        if (roomIdObj != null) {
            Long roomId = Long.valueOf(roomIdObj.toString());
            Long reservationId = reservationIdObj != null ? Long.valueOf(reservationIdObj.toString()) : null;

            CreateHousekeepingTaskRequest taskReq = CreateHousekeepingTaskRequest.builder()
                    .roomId(roomId)
                    .reservationId(reservationId)
                    .taskType(TaskType.CHECKOUT_CLEANING)
                    .priority("HIGH")
                    .remarks("Automated checkout cleaning task")
                    .build();

            operationsService.createTask(taskReq);
            log.info("Created checkout cleaning task for roomId: {}", roomId);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ROOM_DIRTY_QUEUE)
    public void handleRoomMarkedDirty(EventEnvelope<Map<String, Object>> event) {
        log.info("Received RoomMarkedDirty event in operations-service: {}", event);
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        Object roomIdObj = payload.get("roomId");
        if (roomIdObj != null) {
            Long roomId = Long.valueOf(roomIdObj.toString());
            CreateHousekeepingTaskRequest taskReq = CreateHousekeepingTaskRequest.builder()
                    .roomId(roomId)
                    .taskType(TaskType.GUEST_REQUEST_CLEANING)
                    .priority("NORMAL")
                    .remarks("Room marked dirty task")
                    .build();

            operationsService.createTask(taskReq);
            log.info("Created cleaning task for dirty roomId: {}", roomId);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SERVICE_REQUEST_QUEUE)
    public void handleServiceRequestCreated(EventEnvelope<Map<String, Object>> event) {
        log.info("Received ServiceRequestCreated event in operations-service: {}", event);
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return;

        Object serviceReqIdObj = payload.get("serviceRequestId");
        Object roomIdObj = payload.get("roomId");
        Object reservationIdObj = payload.get("reservationId");
        Object reqTypeObj = payload.get("requestType");
        Object descObj = payload.get("description");

        if (roomIdObj != null && reqTypeObj != null) {
            Long roomId = Long.valueOf(roomIdObj.toString());
            Long serviceRequestId = serviceReqIdObj != null ? Long.valueOf(serviceReqIdObj.toString()) : null;
            Long reservationId = reservationIdObj != null ? Long.valueOf(reservationIdObj.toString()) : null;
            String reqTypeStr = reqTypeObj.toString();
            String desc = descObj != null ? descObj.toString() : "";

            TaskType taskType;
            switch (reqTypeStr) {
                case "FOOD_ORDER":
                    taskType = TaskType.ROOM_SERVICE;
                    break;
                case "AMENITY":
                case "EXTRA_TOWELS":
                    taskType = TaskType.AMENITY_DELIVERY;
                    break;
                case "LAUNDRY":
                    taskType = TaskType.LAUNDRY;
                    break;
                case "ROOM_CLEANING":
                    taskType = TaskType.GUEST_REQUEST_CLEANING;
                    break;
                default:
                    taskType = TaskType.GUEST_REQUEST_CLEANING;
                    break;
            }

            CreateHousekeepingTaskRequest taskReq = CreateHousekeepingTaskRequest.builder()
                    .roomId(roomId)
                    .reservationId(reservationId)
                    .serviceRequestId(serviceRequestId)
                    .taskType(taskType)
                    .priority("HIGH")
                    .remarks("Created from guest request: " + desc)
                    .build();

            operationsService.createTask(taskReq);
            log.info("Created housekeeping task type {} for serviceRequestId: {}", taskType, serviceRequestId);
        }
    }
}
