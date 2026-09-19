package com.hms.operationsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "room-service", fallback = RoomClientFallback.class)
public interface RoomClient {

    @GetMapping("/api/rooms/{id}")
    Map<String, Object> getRoomById(@PathVariable("id") Long id);

    @PutMapping("/api/rooms/{id}/status")
    Map<String, Object> updateRoomStatus(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @GetMapping("/api/halls/{id}")
    Map<String, Object> getHallById(@PathVariable("id") Long id);

    @PutMapping("/api/halls/{id}/status")
    Map<String, Object> updateHallStatus(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);
}

