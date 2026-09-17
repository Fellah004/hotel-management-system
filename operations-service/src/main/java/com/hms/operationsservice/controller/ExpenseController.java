package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.CreateExpenseRequest;
import com.hms.operationsservice.dto.response.ExpenseResponse;
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
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Hotel Expenses", description = "APIs for recording and tracking operational expenses")
@SecurityRequirement(name = "BearerAuth")
public class ExpenseController {

    private final OperationsService operationsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Record expense", description = "Records a new operational expense")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return new ResponseEntity<>(operationsService.createExpense(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "List expenses", description = "Retrieves all recorded operational expenses")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        return ResponseEntity.ok(operationsService.getAllExpenses());
    }
}
