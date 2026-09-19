package com.hms.operationsservice.repository;

import com.hms.operationsservice.entity.CashDrawer;
import com.hms.operationsservice.entity.CashDrawerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashDrawerRepository extends JpaRepository<CashDrawer, Long> {
    List<CashDrawer> findByDrawerDateAndStatus(LocalDate drawerDate, CashDrawerStatus status);
    Optional<CashDrawer> findFirstByOrderByCreatedAtDesc();
}
