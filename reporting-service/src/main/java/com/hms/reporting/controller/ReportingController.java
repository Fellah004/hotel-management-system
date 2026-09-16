package com.hms.reporting.controller;
import com.hms.reporting.dto.*; import com.hms.reporting.entity.ReportType; import com.hms.reporting.service.ReportingService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ReportingController {
 private final ReportingService service;
 @PostMapping public ResponseEntity<ReportResponse> create(@Valid @RequestBody ReportRequest r){return ResponseEntity.ok(service.create(r));}
 @GetMapping("/{id}") public ResponseEntity<ReportResponse> get(@PathVariable UUID id){return ResponseEntity.ok(service.get(id));}
 @GetMapping public ResponseEntity<List<ReportResponse>> type(@RequestParam ReportType type){return ResponseEntity.ok(service.type(type));}
 @GetMapping("/range") public ResponseEntity<List<ReportResponse>> range(@RequestParam LocalDate from,@RequestParam LocalDate to){return ResponseEntity.ok(service.range(from,to));}
 @GetMapping("/occupancy") public ResponseEntity<List<ReportResponse>> occupancy(){return ResponseEntity.ok(service.type(ReportType.OCCUPANCY));}
 @GetMapping("/revenue") public ResponseEntity<List<ReportResponse>> revenue(){return ResponseEntity.ok(service.type(ReportType.REVENUE));}
 @GetMapping("/expense") public ResponseEntity<List<ReportResponse>> expense(){return ResponseEntity.ok(service.type(ReportType.EXPENSE));}
 @GetMapping("/profit") public ResponseEntity<List<ReportResponse>> profit(){return ResponseEntity.ok(service.type(ReportType.PROFIT));}
 @GetMapping("/staff-performance") public ResponseEntity<List<ReportResponse>> staff(){return ResponseEntity.ok(service.type(ReportType.STAFF_PERFORMANCE));}
 @GetMapping("/daily-summary") public ResponseEntity<List<ReportResponse>> daily(){return ResponseEntity.ok(service.type(ReportType.DAILY_SUMMARY));}
 @GetMapping("/peak-booking") public ResponseEntity<List<ReportResponse>> peak(){return ResponseEntity.ok(service.type(ReportType.PEAK_BOOKING));}
 @GetMapping("/low-stock") public ResponseEntity<List<ReportResponse>> low(){return ResponseEntity.ok(service.type(ReportType.LOW_STOCK));}
 @GetMapping("/latest") public ResponseEntity<ReportResponse> latest(@RequestParam ReportType type,@RequestParam LocalDate date){return ResponseEntity.ok(service.latest(type,date));}
}