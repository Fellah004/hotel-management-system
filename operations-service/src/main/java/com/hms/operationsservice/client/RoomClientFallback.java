package com.hms.operationsservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
public class RoomClientFallback implements RoomClient {

    @Override
    public Map<String, Object> getRoomById(Long id) {
        log.warn("Fallback triggered: Failed to fetch room details for room id: {}", id);
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> updateRoomStatus(Long id, Map<String, Object> request) {
        log.warn("Fallback triggered: Failed to update room status for room id: {} to: {}", id, request);
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getHallById(Long id) {
        log.warn("Fallback triggered: Failed to fetch hall details for hall id: {}", id);
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> updateHallStatus(Long id, Map<String, Object> request) {
        log.warn("Fallback triggered: Failed to update hall status for hall id: {} to: {}", id, request);
        return Collections.emptyMap();
    }
}

