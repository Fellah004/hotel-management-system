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
}
