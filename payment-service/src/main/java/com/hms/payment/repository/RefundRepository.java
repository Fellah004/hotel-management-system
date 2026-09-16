package com.hms.payment.repository;
import com.hms.payment.entity.Refund; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface RefundRepository extends JpaRepository<Refund,UUID> { List<Refund> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId); }