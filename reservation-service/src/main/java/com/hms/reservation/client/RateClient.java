package com.hms.reservationservice.client;

import com.hms.reservationservice.client.dto.RateQuoteRequestDto;
import com.hms.reservationservice.client.dto.RateQuoteResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "rate-service")
public interface RateClient {

    @PostMapping("/api/rates/quote")
    RateQuoteResponseDto calculateQuote(@RequestBody RateQuoteRequestDto request);
}
