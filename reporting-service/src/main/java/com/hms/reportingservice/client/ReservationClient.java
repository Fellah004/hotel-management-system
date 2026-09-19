package com.hms.reportingservice.client;

import com.hms.reportingservice.client.dto.ReservationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "reservation-service")
public interface ReservationClient {

    @GetMapping("/api/reservations")
    List<ReservationDto> getAllReservations();
}
