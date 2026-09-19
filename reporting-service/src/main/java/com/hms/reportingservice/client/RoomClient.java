package com.hms.reportingservice.client;

import com.hms.reportingservice.client.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "room-service")
public interface RoomClient {

    @GetMapping("/api/rooms")
    List<RoomDto> getAllRooms(@RequestParam(value = "includeInactive", required = false) Boolean includeInactive);

    @GetMapping("/api/halls")
    List<Map<String, Object>> getAllHalls(@RequestParam(value = "includeInactive", required = false) Boolean includeInactive);
}
