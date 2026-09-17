package com.hms.operationsservice.service;

import com.hms.operationsservice.dto.request.*;
import com.hms.operationsservice.dto.response.*;

import java.util.List;

public interface OperationsService {
    // Housekeeping
    HousekeepingTaskResponse createTask(CreateHousekeepingTaskRequest request);
    List<HousekeepingTaskResponse> getMyTasks(Long staffId);
    HousekeepingTaskResponse getTaskById(Long id);
    HousekeepingTaskResponse acceptTask(Long id, Long staffId);
    HousekeepingTaskResponse startTask(Long id, Long staffId);
    HousekeepingTaskResponse completeTask(Long id, Long staffId);
    HousekeepingTaskResponse rejectTask(Long id, Long staffId, String remarks);
    HousekeepingTaskResponse verifyTask(Long id, Long managerId);
    List<TaskHistoryResponse> getTaskHistory(Long taskId);

    // Room Assignments
    RoomAssignmentResponse createRoomAssignment(CreateRoomAssignmentRequest request);
    List<RoomAssignmentResponse> getAllRoomAssignments();
    RoomAssignmentResponse getRoomAssignmentById(Long id);
    void deleteRoomAssignment(Long id);

    // Maintenance
    MaintenanceResponse createMaintenanceIssue(CreateMaintenanceRequest request);
    List<MaintenanceResponse> getAllMaintenanceIssues();
    MaintenanceResponse getMaintenanceIssueById(Long id);
    MaintenanceResponse assignMaintenance(Long id, AssignMaintenanceRequest request);
    MaintenanceResponse updateMaintenanceStatus(Long id, UpdateMaintenanceStatusRequest request);

    // Breakage
    BreakageResponse reportBreakage(ReportBreakageRequest request);
    List<BreakageResponse> getAllBreakageReports();
    BreakageResponse reviewBreakage(Long id, ReviewBreakageRequest request);

    // Expenses
    ExpenseResponse createExpense(CreateExpenseRequest request);
    List<ExpenseResponse> getAllExpenses();

    // Utilities
    UtilityResponse createUtilityRecord(CreateUtilityRequest request);
    List<UtilityResponse> getUtilityRecords(Integer year, Integer month);

    // Cash Drawer
    CashDrawerResponse openCashDrawer(OpenCashDrawerRequest request);
    CashDrawerResponse closeCashDrawer(Long id, CloseCashDrawerRequest request);
    CashDrawerResponse getCurrentCashDrawer();

    // Shift Handover
    ShiftHandoverResponse createShiftHandover(CreateShiftHandoverRequest request);
    ShiftHandoverResponse acknowledgeShiftHandover(Long id, AcknowledgeHandoverRequest request);
    List<ShiftHandoverResponse> getShiftHandovers(Long staffId);
}
