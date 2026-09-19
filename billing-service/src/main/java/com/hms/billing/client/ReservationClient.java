package com.hms.billingservice.client;

import com.hms.billingservice.client.dto.ReservationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reservation-service")
public interface ReservationClient {

    @GetMapping("/api/reservations/{id}")
    ReservationDto getReservationById(@PathVariable("id") Long id);
}
