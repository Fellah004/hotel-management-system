package com.hms.operationsservice.controller;

import com.hms.operationsservice.dto.request.CreateHousekeepingTaskRequest;
import com.hms.operationsservice.dto.response.HousekeepingTaskResponse;
import com.hms.operationsservice.dto.response.TaskHistoryResponse;
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
@RequestMapping("/api/housekeeping/tasks")
@RequiredArgsConstructor
@Tag(name = "Housekeeping Operations", description = "APIs for housekeeping tasks, room cleaning, amenity deliveries, and verification")
@SecurityRequirement(name = "BearerAuth")
public class HousekeepingController {

    private final OperationsService operationsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    @Operation(summary = "Create task", description = "Manually creates a housekeeping task")
    public ResponseEntity<HousekeepingTaskResponse> createTask(@Valid @RequestBody CreateHousekeepingTaskRequest request) {
        return new ResponseEntity<>(operationsService.createTask(request), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('HOUSEKEEPER', 'MANAGER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get my assigned tasks", description = "Retrieves tasks assigned to a specific housekeeper")
    public ResponseEntity<List<HousekeepingTaskResponse>> getMyTasks(@RequestParam(value = "staffId", required = false) Long staffId) {
        return ResponseEntity.ok(operationsService.getMyTasks(staffId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HOUSEKEEPER', 'MANAGER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get task by ID", description = "Retrieves task details")
    public ResponseEntity<HousekeepingTaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(operationsService.getTaskById(id));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('HOUSEKEEPER', 'MANAGER')")
    @Operation(summary = "Accept task", description = "Housekeeper accepts the assigned task")
    public ResponseEntity<HousekeepingTaskResponse> acceptTask(@PathVariable Long id,
                                                               @RequestParam(value = "staffId", required = false) Long staffId) {
        return ResponseEntity.ok(operationsService.acceptTask(id, staffId));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('HOUSEKEEPER', 'MANAGER')")
    @Operation(summary = "Start task", description = "Housekeeper starts task; marks room CLEANING")
    public ResponseEntity<HousekeepingTaskResponse> startTask(@PathVariable Long id,
                                                              @RequestParam(value = "staffId", required = false) Long staffId) {
        return ResponseEntity.ok(operationsService.startTask(id, staffId));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('HOUSEKEEPER', 'MANAGER')")
    @Operation(summary = "Complete task", description = "Housekeeper completes task; marks room CLEAN")
    public ResponseEntity<HousekeepingTaskResponse> completeTask(@PathVariable Long id,
                                                                 @RequestParam(value = "staffId", required = false) Long staffId) {
        return ResponseEntity.ok(operationsService.completeTask(id, staffId));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('HOUSEKEEPER', 'MANAGER')")
    @Operation(summary = "Reject task", description = "Housekeeper rejects assigned task with reason")
    public ResponseEntity<HousekeepingTaskResponse> rejectTask(@PathVariable Long id,
                                                               @RequestParam(value = "staffId", required = false) Long staffId,
                                                               @RequestParam(value = "remarks", defaultValue = "Rejected by housekeeper") String remarks) {
        return ResponseEntity.ok(operationsService.rejectTask(id, staffId, remarks));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Verify task", description = "Manager inspects and verifies clean room; marks room AVAILABLE")
    public ResponseEntity<HousekeepingTaskResponse> verifyTask(@PathVariable Long id,
                                                               @RequestParam(value = "managerId", defaultValue = "1") Long managerId) {
        return ResponseEntity.ok(operationsService.verifyTask(id, managerId));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('HOUSEKEEPER', 'MANAGER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get task history", description = "Retrieves task lifecycle history")
    public ResponseEntity<List<TaskHistoryResponse>> getTaskHistory(@PathVariable Long id) {
        return ResponseEntity.ok(operationsService.getTaskHistory(id));
    }
}
