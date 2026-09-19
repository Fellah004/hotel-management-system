package com.hms.reportingservice.repository;

import com.hms.reportingservice.entity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
    Optional<DailySummary> findBySummaryDate(LocalDate summaryDate);
    List<DailySummary> findTop30ByOrderBySummaryDateDesc();
    List<DailySummary> findBySummaryDateBetweenOrderBySummaryDateAsc(LocalDate startDate, LocalDate endDate);
}
