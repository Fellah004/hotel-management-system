package com.hms.reservationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "billing-service")
public interface BillingClient {

    @PostMapping("/api/bills")
    Map<String, Object> createBill(@RequestBody Map<String, Object> request);

    @GetMapping("/api/bills/reservation/{reservationId}")
    Map<String, Object> getBillByReservationId(@PathVariable("reservationId") Long reservationId);
}
