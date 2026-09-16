package com.hms.payment.repository;
import com.hms.payment.entity.Payment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment,UUID> {
 Optional<Payment> findByIdempotencyKey(String key);
 List<Payment> findByReservationIdOrderByCreatedAtDesc(UUID reservationId);
 List<Payment> findByGuestIdOrderByCreatedAtDesc(UUID guestId);
}