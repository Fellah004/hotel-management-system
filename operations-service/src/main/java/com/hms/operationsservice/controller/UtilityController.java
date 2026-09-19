package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.CreateUtilityRequest;
import com.hms.operationsservice.dto.response.UtilityResponse;
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
@RequestMapping("/api/utilities")
@RequiredArgsConstructor
@Tag(name = "Utility Consumption & Costs", description = "APIs for logging electricity, water, gas consumption and costs")
@SecurityRequirement(name = "BearerAuth")
public class UtilityController {

    private final OperationsService operationsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Record utility usage", description = "Logs monthly utility units and costs")
    public ResponseEntity<UtilityResponse> createUtilityRecord(@Valid @RequestBody CreateUtilityRequest request) {
        return new ResponseEntity<>(operationsService.createUtilityRecord(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "List utility records", description = "Retrieves utility records, optionally filtered by year and month")
    public ResponseEntity<List<UtilityResponse>> getUtilityRecords(@RequestParam(value = "year", required = false) Integer year,
                                                                   @RequestParam(value = "month", required = false) Integer month) {
        return ResponseEntity.ok(operationsService.getUtilityRecords(year, month));
    }
}
