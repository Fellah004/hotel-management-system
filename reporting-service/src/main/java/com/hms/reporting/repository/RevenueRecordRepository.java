package com.hms.reportingservice.repository;

import com.hms.reportingservice.entity.RevenueRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RevenueRecordRepository extends JpaRepository<RevenueRecord, Long> {

    List<RevenueRecord> findByRecordDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RevenueRecord r WHERE r.recordDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RevenueRecord r WHERE r.recordDate = :date")
    BigDecimal calculateTotalRevenueForDate(@Param("date") LocalDate date);
}
