package com.hms.operationsservice.repository;

import com.hms.operationsservice.entity.BreakageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BreakageReportRepository extends JpaRepository<BreakageReport, Long> {
    List<BreakageReport> findByReservationId(Long reservationId);
    List<BreakageReport> findByStatus(String status);
    Optional<BreakageReport> findByReportCode(String reportCode);
}
