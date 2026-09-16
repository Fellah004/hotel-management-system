package com.hms.payment.controller;

import com.hms.payment.entity.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @PostMapping public ResponseEntity<Payment> processPayment(@RequestBody Payment payment){ return ResponseEntity.ok(payment); }
    @GetMapping("/{paymentId}") public ResponseEntity<Payment> getPayment(@PathVariable UUID paymentId){ Payment p=new Payment(); p.setId(paymentId); return ResponseEntity.ok(p); }
    @GetMapping("/{paymentId}/confirmation") public ResponseEntity<Payment> getPaymentConfirmation(@PathVariable UUID paymentId){ Payment p=new Payment(); p.setId(paymentId); return ResponseEntity.ok(p); }
    @GetMapping("/history") public ResponseEntity<List<Payment>> getPaymentHistory(@RequestParam(required=false) UUID guestId,@RequestParam(required=false) UUID reservationId){ return ResponseEntity.ok(List.of()); }
    @PostMapping("/check-in") public ResponseEntity<Payment> collectPaymentDuringCheckIn(@RequestBody Payment payment){ return ResponseEntity.ok(payment); }
    @PostMapping("/check-out") public ResponseEntity<Payment> collectPaymentDuringCheckOut(@RequestBody Payment payment){ return ResponseEntity.ok(payment); }
}
