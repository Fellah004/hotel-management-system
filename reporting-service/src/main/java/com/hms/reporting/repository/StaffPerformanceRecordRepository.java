package com.hms.reportingservice.repository;

import com.hms.reportingservice.entity.StaffPerformanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffPerformanceRecordRepository extends JpaRepository<StaffPerformanceRecord, Long> {
    Optional<StaffPerformanceRecord> findByStaffIdAndRecordDate(Long staffId, LocalDate recordDate);
    List<StaffPerformanceRecord> findByRecordDateBetween(LocalDate startDate, LocalDate endDate);
    List<StaffPerformanceRecord> findByStaffId(Long staffId);
}
