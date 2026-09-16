package com.hms.reporting.repository;
import com.hms.reporting.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.*; import java.util.*;
public interface ReportSnapshotRepository extends JpaRepository<ReportSnapshot,UUID> {
 List<ReportSnapshot> findByReportTypeOrderByReportDateDesc(ReportType type);
 List<ReportSnapshot> findByReportDateBetweenOrderByReportDateAsc(LocalDate from,LocalDate to);
 Optional<ReportSnapshot> findTopByReportTypeAndReportDateOrderByCreatedAtDesc(ReportType type,LocalDate date);
}