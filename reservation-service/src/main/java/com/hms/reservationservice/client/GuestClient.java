package com.hms.reservationservice.client;

import com.hms.reservationservice.client.dto.GuestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "guest-service")
public interface GuestClient {

    @GetMapping("/api/guests/{id}")
    GuestDto getGuestById(@PathVariable("id") Long id);
}
