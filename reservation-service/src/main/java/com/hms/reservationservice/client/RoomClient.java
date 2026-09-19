package com.hms.reservationservice.client;

import com.hms.reservationservice.client.dto.RoomDto;
import com.hms.reservationservice.client.dto.RoomStatusUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "room-service")
public interface RoomClient {

    @GetMapping("/api/rooms/{id}")
    RoomDto getRoomById(@PathVariable("id") Long id);

    @PutMapping("/api/rooms/{id}/status")
    RoomDto updateRoomStatus(@PathVariable("id") Long id, @RequestBody RoomStatusUpdateDto request);

    @GetMapping("/api/halls/{id}")
    java.util.Map<String, Object> getHallById(@PathVariable("id") Long id);

    @PutMapping("/api/halls/{id}/status")
    java.util.Map<String, Object> updateHallStatus(@PathVariable("id") Long id, @RequestBody java.util.Map<String, Object> request);

    @GetMapping("/api/room-categories/{id}")
    java.util.Map<String, Object> getRoomCategoryById(@PathVariable("id") Long id);

    @GetMapping("/api/hall-categories/{id}")
    java.util.Map<String, Object> getHallCategoryById(@PathVariable("id") Long id);

    @GetMapping("/api/rooms/available")
    java.util.List<RoomDto> getAvailableRooms(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                              @RequestParam(value = "minCapacity", required = false) Integer minCapacity);
}
