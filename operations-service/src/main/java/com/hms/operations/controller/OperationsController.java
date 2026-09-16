package com.hms.operations.controller;

import com.hms.operations.entity.Expense;
import com.hms.operations.entity.HousekeepingTask;
import com.hms.operations.entity.MaintenanceRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operations")
public class OperationsController {

    @PostMapping("/housekeeping")
    public ResponseEntity<HousekeepingTask> createHousekeepingTask(
            @Valid @RequestBody HousekeepingTask task) {

        return ResponseEntity.ok(task);
    }

    @GetMapping("/housekeeping")
    public ResponseEntity<List<HousekeepingTask>> getHousekeepingTasks() {
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/maintenance")
    public ResponseEntity<MaintenanceRequest> createMaintenanceRequest(
            @Valid @RequestBody MaintenanceRequest request) {

        return ResponseEntity.ok(request);
    }

    @GetMapping("/maintenance")
    public ResponseEntity<List<MaintenanceRequest>> getMaintenanceRequests() {
        return ResponseEntity.ok(List.of());
    }

    @PutMapping("/maintenance/{id}/status")
    public ResponseEntity<String> updateMaintenanceStatus(
            @PathVariable UUID id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                "Maintenance request " + id + " status changed to " + status
        );
    }

    @PostMapping("/expenses")
    public ResponseEntity<Expense> createExpense(
            @Valid @RequestBody Expense expense) {

        return ResponseEntity.ok(expense);
    }

    @GetMapping("/expenses")
    public ResponseEntity<List<Expense>> getExpenses() {
        return ResponseEntity.ok(List.of());
    }
}
