package com.hms.operationsservice.repository;

import com.hms.operationsservice.entity.UtilityRecord;
import com.hms.operationsservice.entity.UtilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilityRecordRepository extends JpaRepository<UtilityRecord, Long> {
    List<UtilityRecord> findByPeriodYearAndPeriodMonth(Integer year, Integer month);
    List<UtilityRecord> findByUtilityType(UtilityType utilityType);
}
