package com.hms.billingservice.repository;

import com.hms.billingservice.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByReservationId(Long reservationId);
    Optional<Bill> findByBillingNumber(String billingNumber);
}
