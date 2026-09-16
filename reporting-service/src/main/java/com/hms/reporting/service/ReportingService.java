package com.hms.reporting.service;
import com.hms.reporting.dto.*; import com.hms.reporting.entity.*; import com.hms.reporting.repository.ReportSnapshotRepository; import jakarta.persistence.EntityNotFoundException; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import java.time.*; import java.util.*;
@Service @RequiredArgsConstructor
public class ReportingService {
 private final ReportSnapshotRepository repo;
 public ReportResponse create(ReportRequest r){return map(repo.save(ReportSnapshot.builder().reportType(r.getReportType()).reportDate(r.getReportDate()).periodLabel(r.getPeriodLabel()).value(r.getValue()).summary(r.getSummary()).build()));}
 public ReportResponse get(UUID id){return map(repo.findById(id).orElseThrow(()->new EntityNotFoundException("Report not found: "+id)));}
 public List<ReportResponse> type(ReportType t){return repo.findByReportTypeOrderByReportDateDesc(t).stream().map(this::map).toList();}
 public List<ReportResponse> range(LocalDate f,LocalDate t){return repo.findByReportDateBetweenOrderByReportDateAsc(f,t).stream().map(this::map).toList();}
 public ReportResponse latest(ReportType t,LocalDate d){return map(repo.findTopByReportTypeAndReportDateOrderByCreatedAtDesc(t,d).orElseThrow(()->new EntityNotFoundException("No report found for "+d)));}
 private ReportResponse map(ReportSnapshot r){return ReportResponse.builder().id(r.getId()).reportType(r.getReportType()).reportDate(r.getReportDate()).periodLabel(r.getPeriodLabel()).value(r.getValue()).summary(r.getSummary()).createdAt(r.getCreatedAt()).build();}
}