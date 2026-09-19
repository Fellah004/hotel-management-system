package com.hms.billingservice.client;

import com.hms.billingservice.client.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "room-service")
public interface RoomClient {

    @GetMapping("/api/rooms/{id}")
    RoomDto getRoomById(@PathVariable("id") Long id);

    @GetMapping("/api/room-categories/{id}")
    Map<String, Object> getRoomCategoryById(@PathVariable("id") Long id);

    @GetMapping("/api/halls/{id}")
    Map<String, Object> getHallById(@PathVariable("id") Long id);

    @GetMapping("/api/hall-categories/{id}")
    Map<String, Object> getHallCategoryById(@PathVariable("id") Long id);
}
