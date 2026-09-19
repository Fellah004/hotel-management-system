package com.hms.paymentservice.repository;

import com.hms.paymentservice.entity.Payment;
import com.hms.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findByReservationId(Long reservationId);
    List<Payment> findByStatus(PaymentStatus status);
}
