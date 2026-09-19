package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.AcknowledgeHandoverRequest;
import com.hms.operationsservice.dto.request.CreateShiftHandoverRequest;
import com.hms.operationsservice.dto.response.ShiftHandoverResponse;
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
@RequestMapping("/api/shift-handover")
@RequiredArgsConstructor
@Tag(name = "Shift Handover Operations", description = "APIs for transferring pending hotel tasks, arrivals, departures, complaints, and cash between shifts")
@SecurityRequirement(name = "BearerAuth")
public class ShiftHandoverController {

    private final OperationsService operationsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Create shift handover", description = "Logs pending tasks and handover notes for next shift")
    public ResponseEntity<ShiftHandoverResponse> createShiftHandover(@Valid @RequestBody CreateShiftHandoverRequest request) {
        return new ResponseEntity<>(operationsService.createShiftHandover(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Acknowledge shift handover", description = "Incoming staff acknowledges receipt of shift handover")
    public ResponseEntity<ShiftHandoverResponse> acknowledgeShiftHandover(@PathVariable Long id,
                                                                          @Valid @RequestBody AcknowledgeHandoverRequest request) {
        return ResponseEntity.ok(operationsService.acknowledgeShiftHandover(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "List shift handovers", description = "Retrieves shift handovers, optionally filtered by staffId")
    public ResponseEntity<List<ShiftHandoverResponse>> getShiftHandovers(@RequestParam(value = "staffId", required = false) Long staffId) {
        return ResponseEntity.ok(operationsService.getShiftHandovers(staffId));
    }
}
