package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.ReportBreakageRequest;
import com.hms.operationsservice.dto.request.ReviewBreakageRequest;
import com.hms.operationsservice.dto.response.BreakageResponse;
import com.hms.operationsservice.service.OperationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breakage")
@RequiredArgsConstructor
@Tag(name = "Breakage & Damage Reports", description = "APIs for reporting breakage and manager approval/billing triggers")
@SecurityRequirement(name = "BearerAuth")
public class BreakageController {

    private final OperationsService operationsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'HOUSEKEEPER')")
    @Operation(summary = "Report breakage/damage", description = "Reports item damage in a room")
    public ResponseEntity<BreakageResponse> reportBreakage(@Valid @RequestBody ReportBreakageRequest request) {
        return new ResponseEntity<>(operationsService.reportBreakage(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "List breakage reports", description = "Retrieves all breakage reports")
    public ResponseEntity<List<BreakageResponse>> getAllBreakageReports() {
        return ResponseEntity.ok(operationsService.getAllBreakageReports());
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Review breakage report", description = "Approves or rejects damage charge; publishes BreakageApproved event")
    public ResponseEntity<BreakageResponse> reviewBreakage(@PathVariable Long id,
                                                           @Valid @RequestBody ReviewBreakageRequest request) {
        return ResponseEntity.ok(operationsService.reviewBreakage(id, request));
    }
}
