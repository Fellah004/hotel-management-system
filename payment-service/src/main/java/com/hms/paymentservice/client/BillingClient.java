package com.hms.paymentservice.client;

import com.hms.paymentservice.client.dto.BillDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "billing-service")
public interface BillingClient {

    @GetMapping("/api/bills/reservation/{reservationId}")
    BillDto getBillByReservationId(@PathVariable("reservationId") Long reservationId);

    @org.springframework.web.bind.annotation.PostMapping("/api/bills/reservation/{reservationId}/record-payment")
    BillDto recordPaymentForReservation(@PathVariable("reservationId") Long reservationId,
                                        @org.springframework.web.bind.annotation.RequestParam("amount") java.math.BigDecimal amount);
}
