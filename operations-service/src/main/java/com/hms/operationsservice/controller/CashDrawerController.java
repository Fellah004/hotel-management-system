package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.CloseCashDrawerRequest;
import com.hms.operationsservice.dto.request.OpenCashDrawerRequest;
import com.hms.operationsservice.dto.response.CashDrawerResponse;
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

@RestController
@RequestMapping("/api/cash-drawer")
@RequiredArgsConstructor
@Tag(name = "Front Desk Cash Drawer", description = "APIs for opening, tracking, reconciling, and closing cash drawers per shift")
@SecurityRequirement(name = "BearerAuth")
public class CashDrawerController {

    private final OperationsService operationsService;

    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Open cash drawer", description = "Opens a cash drawer with opening balance")
    public ResponseEntity<CashDrawerResponse> openCashDrawer(@Valid @RequestBody OpenCashDrawerRequest request) {
        return new ResponseEntity<>(operationsService.openCashDrawer(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Close cash drawer", description = "Reconciles cash received/expenses vs actual closing cash")
    public ResponseEntity<CashDrawerResponse> closeCashDrawer(@PathVariable Long id,
                                                              @Valid @RequestBody CloseCashDrawerRequest request) {
        return ResponseEntity.ok(operationsService.closeCashDrawer(id, request));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Get current cash drawer", description = "Retrieves current active cash drawer status")
    public ResponseEntity<CashDrawerResponse> getCurrentCashDrawer() {
        return ResponseEntity.ok(operationsService.getCurrentCashDrawer());
    }
}
