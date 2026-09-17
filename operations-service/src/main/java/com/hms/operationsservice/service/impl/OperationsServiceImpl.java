package com.hms.operationsservice.service.impl;

import com.hms.operationsservice.client.RoomClient;
import com.hms.operationsservice.dto.request.*;
import com.hms.operationsservice.dto.response.*;
import com.hms.operationsservice.entity.*;
import com.hms.operationsservice.event.publisher.OperationsEventPublisher;
import com.hms.operationsservice.exception.BusinessRuleException;
import com.hms.operationsservice.exception.ForbiddenException;
import com.hms.operationsservice.exception.ResourceNotFoundException;
import com.hms.operationsservice.repository.*;
import com.hms.operationsservice.service.OperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationsServiceImpl implements OperationsService {

    private final HousekeepingTaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final RoomAssignmentRepository roomAssignmentRepository;
    private final MaintenanceIssueRepository maintenanceRepository;
    private final BreakageReportRepository breakageRepository;
    private final ExpenseRepository expenseRepository;
    private final UtilityRecordRepository utilityRepository;
    private final CashDrawerRepository cashDrawerRepository;
    private final ShiftHandoverRepository shiftHandoverRepository;
    private final OperationsEventPublisher eventPublisher;
    private final RoomClient roomClient;

    // --- Housekeeping ---

    @Override
    @Transactional
    public HousekeepingTaskResponse createTask(CreateHousekeepingTaskRequest request) {
        log.info("Creating housekeeping task type: {} for roomId: {}", request.getTaskType(), request.getRoomId());

        String code = "TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Long staffId = request.getAssignedStaffId();
        TaskStatus status = TaskStatus.UNASSIGNED;

        // Auto-assign from room assignment if not provided
        if (staffId == null) {
            Integer roomNumber = null;
            try {
                Map<String, Object> room = roomClient.getRoomById(request.getRoomId());
                if (room != null && room.get("roomNumber") != null) {
                    roomNumber = Integer.parseInt(room.get("roomNumber").toString());
                }
            } catch (Exception e) {
                log.warn("Could not fetch room details for roomId: {}", request.getRoomId());
            }
            if (roomNumber == null) {
                roomNumber = request.getRoomId().intValue();
            }

            List<RoomAssignment> assignments = roomAssignmentRepository.findActiveAssignmentsForRoom(roomNumber, LocalDate.now());
            if (!assignments.isEmpty()) {
                staffId = assignments.get(0).getStaffId();
                status = TaskStatus.ASSIGNED;
            }
        } else {
            status = TaskStatus.ASSIGNED;
        }

        HousekeepingTask task = HousekeepingTask.builder()
                .taskCode(code)
                .roomId(request.getRoomId())
                .reservationId(request.getReservationId())
                .serviceRequestId(request.getServiceRequestId())
                .taskType(request.getTaskType())
                .assignedStaffId(staffId)
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .status(status)
                .remarks(request.getRemarks())
                .build();

        HousekeepingTask saved = taskRepository.save(task);

        recordTaskHistory(saved.getId(), null, status, "SYSTEM", "Task created");

        if (status == TaskStatus.ASSIGNED) {
            eventPublisher.publishTaskAssigned(saved);
        }

        return mapToTaskResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HousekeepingTaskResponse> getMyTasks(Long staffId) {
        if (staffId != null) {
            return taskRepository.findByAssignedStaffId(staffId).stream()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        }
        return taskRepository.findAll().stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HousekeepingTaskResponse getTaskById(Long id) {
        HousekeepingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return mapToTaskResponse(task);
    }

    @Override
    @Transactional
    public HousekeepingTaskResponse acceptTask(Long id, Long staffId) {
        HousekeepingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        validateTaskOwnership(task, staffId);

        if (task.getStatus() != TaskStatus.ASSIGNED && task.getStatus() != TaskStatus.UNASSIGNED) {
            throw new BusinessRuleException("Cannot accept task with status: " + task.getStatus());
        }

        if (task.getAssignedStaffId() == null && staffId != null) {
            task.setAssignedStaffId(staffId);
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.ACCEPTED);
        HousekeepingTask saved = taskRepository.save(task);

        recordTaskHistory(saved.getId(), oldStatus, TaskStatus.ACCEPTED, "STAFF_" + (staffId != null ? staffId : "DEFAULT"), "Task accepted");
        return mapToTaskResponse(saved);
    }

    @Override
    @Transactional
    public HousekeepingTaskResponse startTask(Long id, Long staffId) {
        HousekeepingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        validateTaskOwnership(task, staffId);

        if (task.getStatus() != TaskStatus.ACCEPTED && task.getStatus() != TaskStatus.ASSIGNED) {
            throw new BusinessRuleException("Cannot start task with status: " + task.getStatus());
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());
        HousekeepingTask saved = taskRepository.save(task);

        recordTaskHistory(saved.getId(), oldStatus, TaskStatus.IN_PROGRESS, "STAFF_" + staffId, "Task started");

        // Sync room status to CLEANING only for cleaning tasks
        if (isCleaningTask(saved.getTaskType())) {
            updateRoomStatus(saved.getRoomId(), "CLEANING");
        }

        return mapToTaskResponse(saved);
    }

    @Override
    @Transactional
    public HousekeepingTaskResponse completeTask(Long id, Long staffId) {
        HousekeepingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        validateTaskOwnership(task, staffId);

        if (task.getStatus() != TaskStatus.IN_PROGRESS && task.getStatus() != TaskStatus.ACCEPTED) {
            throw new BusinessRuleException("Cannot complete task with status: " + task.getStatus());
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        HousekeepingTask saved = taskRepository.save(task);

        recordTaskHistory(saved.getId(), oldStatus, TaskStatus.COMPLETED, "STAFF_" + staffId, "Task completed");

        // Sync room status to CLEAN only for cleaning tasks
        if (isCleaningTask(saved.getTaskType())) {
            updateRoomStatus(saved.getRoomId(), "CLEAN");
        }

        // Publish event
        eventPublisher.publishTaskCompleted(saved);

        return mapToTaskResponse(saved);
    }

    @Override
    @Transactional
    public HousekeepingTaskResponse rejectTask(Long id, Long staffId, String remarks) {
        HousekeepingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        validateTaskOwnership(task, staffId);

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.REJECTED);
        task.setRemarks(remarks);
        task.setAssignedStaffId(null);
        HousekeepingTask saved = taskRepository.save(task);

        recordTaskHistory(saved.getId(), oldStatus, TaskStatus.REJECTED, "STAFF_" + staffId, remarks);
        return mapToTaskResponse(saved);
    }

    @Override
    @Transactional
    public HousekeepingTaskResponse verifyTask(Long id, Long managerId) {
        HousekeepingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new BusinessRuleException("Cannot verify uncompleted task. Current status: " + task.getStatus());
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.VERIFIED);
        task.setVerifiedAt(LocalDateTime.now());
        HousekeepingTask saved = taskRepository.save(task);

        recordTaskHistory(saved.getId(), oldStatus, TaskStatus.VERIFIED, "MANAGER_" + managerId, "Verified by manager");

        // Sync room status to AVAILABLE only for cleaning tasks
        if (isCleaningTask(saved.getTaskType())) {
            updateRoomStatus(saved.getRoomId(), "AVAILABLE");
        }

        return mapToTaskResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getTaskHistory(Long taskId) {
        return taskHistoryRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::mapToTaskHistoryResponse)
                .collect(Collectors.toList());
    }

    // --- Room Assignments ---

    @Override
    @Transactional
    public RoomAssignmentResponse createRoomAssignment(CreateRoomAssignmentRequest request) {
        log.info("Creating room assignment for staffId: {}, rooms {} to {}",
                request.getStaffId(), request.getStartRoomNumber(), request.getEndRoomNumber());

        if (request.getStartRoomNumber() > request.getEndRoomNumber()) {
            throw new BusinessRuleException("startRoomNumber must be <= endRoomNumber");
        }

        RoomAssignment assignment = RoomAssignment.builder()
                .staffId(request.getStaffId())
                .startRoomNumber(request.getStartRoomNumber())
                .endRoomNumber(request.getEndRoomNumber())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        RoomAssignment saved = roomAssignmentRepository.save(assignment);
        return mapToAssignmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomAssignmentResponse> getAllRoomAssignments() {
        return roomAssignmentRepository.findAll().stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomAssignmentResponse getRoomAssignmentById(Long id) {
        RoomAssignment a = roomAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room assignment not found with id: " + id));
        return mapToAssignmentResponse(a);
    }

    @Override
    @Transactional
    public void deleteRoomAssignment(Long id) {
        RoomAssignment a = roomAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room assignment not found with id: " + id));
        roomAssignmentRepository.delete(a);
    }

    // --- Maintenance ---

    @Override
    @Transactional
    public MaintenanceResponse createMaintenanceIssue(CreateMaintenanceRequest request) {
        log.info("Reporting maintenance issue for roomId: {}", request.getRoomId());

        String code = "MAINT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        MaintenanceIssue issue = MaintenanceIssue.builder()
                .issueCode(code)
                .roomId(request.getRoomId())
                .description(request.getDescription())
                .status(MaintenanceStatus.OPEN)
                .cost(BigDecimal.ZERO)
                .reportedBy(request.getReportedBy() != null ? request.getReportedBy() : "STAFF")
                .build();

        MaintenanceIssue saved = maintenanceRepository.save(issue);

        // Put room under MAINTENANCE
        updateRoomStatus(saved.getRoomId(), "MAINTENANCE");

        return mapToMaintenanceResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getAllMaintenanceIssues() {
        return maintenanceRepository.findAll().stream()
                .map(this::mapToMaintenanceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceResponse getMaintenanceIssueById(Long id) {
        MaintenanceIssue issue = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance issue not found with id: " + id));
        return mapToMaintenanceResponse(issue);
    }

    @Override
    @Transactional
    public MaintenanceResponse assignMaintenance(Long id, AssignMaintenanceRequest request) {
        MaintenanceIssue issue = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance issue not found with id: " + id));

        issue.setAssignedStaffId(request.getStaffId());
        issue.setStatus(MaintenanceStatus.ASSIGNED);

        MaintenanceIssue saved = maintenanceRepository.save(issue);
        return mapToMaintenanceResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceResponse updateMaintenanceStatus(Long id, UpdateMaintenanceStatusRequest request) {
        MaintenanceIssue issue = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance issue not found with id: " + id));

        issue.setStatus(request.getStatus());
        if (request.getCost() != null) {
            issue.setCost(request.getCost());
        }
        if (request.getStatus() == MaintenanceStatus.RESOLVED || request.getStatus() == MaintenanceStatus.AVAILABLE) {
            issue.setResolvedAt(LocalDateTime.now());
            if (request.getStatus() == MaintenanceStatus.AVAILABLE) {
                updateRoomStatus(issue.getRoomId(), "AVAILABLE");
            }
        }

        MaintenanceIssue saved = maintenanceRepository.save(issue);
        return mapToMaintenanceResponse(saved);
    }

    // --- Breakage ---

    @Override
    @Transactional
    public BreakageResponse reportBreakage(ReportBreakageRequest request) {
        log.info("Reporting breakage for roomId: {}, reservationId: {}, amount: {}",
                request.getRoomId(), request.getReservationId(), request.getChargeAmount());

        String code = "BRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BreakageReport report = BreakageReport.builder()
                .reportCode(code)
                .reservationId(request.getReservationId())
                .roomId(request.getRoomId())
                .staffId(request.getStaffId())
                .description(request.getDescription())
                .chargeAmount(request.getChargeAmount())
                .status("REPORTED")
                .build();

        BreakageReport saved = breakageRepository.save(report);
        return mapToBreakageResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreakageResponse> getAllBreakageReports() {
        return breakageRepository.findAll().stream()
                .map(this::mapToBreakageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BreakageResponse reviewBreakage(Long id, ReviewBreakageRequest request) {
        BreakageReport report = breakageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Breakage report not found with id: " + id));

        report.setStatus(request.getStatus().toUpperCase());
        report.setApprovedByManagerId(request.getManagerId());
        report.setRemarks(request.getRemarks());

        BreakageReport saved = breakageRepository.save(report);

        if ("APPROVED".equalsIgnoreCase(saved.getStatus())) {
            eventPublisher.publishBreakageApproved(saved);
        }

        return mapToBreakageResponse(saved);
    }

    // --- Expenses ---

    @Override
    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        log.info("Recording expense category: {}, amount: {}", request.getCategory(), request.getAmount());

        Expense expense = Expense.builder()
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .recordedByStaffId(request.getRecordedByStaffId())
                .approvedByManagerId(request.getApprovedByManagerId())
                .status("APPROVED")
                .remarks(request.getRemarks())
                .build();

        Expense saved = expenseRepository.save(expense);
        eventPublisher.publishExpenseCreated(saved);

        return mapToExpenseResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    // --- Utilities ---

    @Override
    @Transactional
    public UtilityResponse createUtilityRecord(CreateUtilityRequest request) {
        log.info("Recording utility type: {}, month: {}, year: {}, cost: {}",
                request.getUtilityType(), request.getPeriodMonth(), request.getPeriodYear(), request.getTotalCost());

        UtilityRecord record = UtilityRecord.builder()
                .utilityType(request.getUtilityType())
                .periodMonth(request.getPeriodMonth())
                .periodYear(request.getPeriodYear())
                .unitsConsumed(request.getUnitsConsumed())
                .totalCost(request.getTotalCost())
                .remarks(request.getRemarks())
                .build();

        UtilityRecord saved = utilityRepository.save(record);
        return mapToUtilityResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilityResponse> getUtilityRecords(Integer year, Integer month) {
        if (year != null && month != null) {
            return utilityRepository.findByPeriodYearAndPeriodMonth(year, month).stream()
                    .map(this::mapToUtilityResponse)
                    .collect(Collectors.toList());
        }
        return utilityRepository.findAll().stream()
                .map(this::mapToUtilityResponse)
                .collect(Collectors.toList());
    }

    // --- Cash Drawer ---

    @Override
    @Transactional
    public CashDrawerResponse openCashDrawer(OpenCashDrawerRequest request) {
        log.info("Opening cash drawer date: {}, staffId: {}, opening balance: {}",
                request.getDrawerDate(), request.getStaffId(), request.getOpeningBalance());

        CashDrawer drawer = CashDrawer.builder()
                .drawerDate(request.getDrawerDate())
                .shiftId(request.getShiftId())
                .openedByStaffId(request.getStaffId())
                .openingBalance(request.getOpeningBalance())
                .cashReceived(BigDecimal.ZERO)
                .cashExpenses(BigDecimal.ZERO)
                .expectedClosing(request.getOpeningBalance())
                .status(CashDrawerStatus.OPEN)
                .remarks(request.getRemarks())
                .build();

        CashDrawer saved = cashDrawerRepository.save(drawer);
        return mapToCashDrawerResponse(saved);
    }

    @Override
    @Transactional
    public CashDrawerResponse closeCashDrawer(Long id, CloseCashDrawerRequest request) {
        log.info("Closing cash drawer id: {}, actual closing: {}", id, request.getActualClosing());

        CashDrawer drawer = cashDrawerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cash drawer not found with id: " + id));

        if (drawer.getStatus() == CashDrawerStatus.CLOSED) {
            throw new BusinessRuleException("Cash drawer is already closed");
        }

        BigDecimal opening = drawer.getOpeningBalance();
        BigDecimal received = request.getCashReceived();
        BigDecimal expenses = request.getCashExpenses();
        BigDecimal expected = opening.add(received).subtract(expenses);
        BigDecimal actual = request.getActualClosing();
        BigDecimal diff = actual.subtract(expected);

        drawer.setCashReceived(received);
        drawer.setCashExpenses(expenses);
        drawer.setExpectedClosing(expected);
        drawer.setActualClosing(actual);
        drawer.setDifference(diff);
        drawer.setStatus(CashDrawerStatus.CLOSED);
        drawer.setClosedByStaffId(request.getStaffId());
        drawer.setClosedAt(LocalDateTime.now());
        if (request.getRemarks() != null) {
            drawer.setRemarks(request.getRemarks());
        }

        CashDrawer saved = cashDrawerRepository.save(drawer);
        return mapToCashDrawerResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CashDrawerResponse getCurrentCashDrawer() {
        CashDrawer drawer = cashDrawerRepository.findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No cash drawer found"));
        return mapToCashDrawerResponse(drawer);
    }

    // --- Shift Handover ---

    @Override
    @Transactional
    public ShiftHandoverResponse createShiftHandover(CreateShiftHandoverRequest request) {
        log.info("Creating shift handover from outgoingStaff: {} to incomingStaff: {}",
                request.getOutgoingStaffId(), request.getIncomingStaffId());

        ShiftHandover handover = ShiftHandover.builder()
                .outgoingStaffId(request.getOutgoingStaffId())
                .incomingStaffId(request.getIncomingStaffId())
                .shiftId(request.getShiftId())
                .pendingPayments(request.getPendingPayments())
                .arrivals(request.getArrivals())
                .departures(request.getDepartures())
                .pendingComplaints(request.getPendingComplaints())
                .pendingServiceRequests(request.getPendingServiceRequests())
                .pendingHousekeepingTasks(request.getPendingHousekeepingTasks())
                .pendingMaintenance(request.getPendingMaintenance())
                .cashInformation(request.getCashInformation())
                .remarks(request.getRemarks())
                .status("PENDING")
                .build();

        ShiftHandover saved = shiftHandoverRepository.save(handover);
        return mapToShiftHandoverResponse(saved);
    }

    @Override
    @Transactional
    public ShiftHandoverResponse acknowledgeShiftHandover(Long id, AcknowledgeHandoverRequest request) {
        ShiftHandover handover = shiftHandoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift handover not found with id: " + id));

        handover.setStatus("ACKNOWLEDGED");
        handover.setAcknowledgedAt(LocalDateTime.now());
        if (request.getRemarks() != null) {
            handover.setRemarks(handover.getRemarks() + " | Acknowledged: " + request.getRemarks());
        }

        ShiftHandover saved = shiftHandoverRepository.save(handover);
        return mapToShiftHandoverResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftHandoverResponse> getShiftHandovers(Long staffId) {
        if (staffId != null) {
            return shiftHandoverRepository.findByOutgoingStaffIdOrIncomingStaffId(staffId, staffId).stream()
                    .map(this::mapToShiftHandoverResponse)
                    .collect(Collectors.toList());
        }
        return shiftHandoverRepository.findAll().stream()
                .map(this::mapToShiftHandoverResponse)
                .collect(Collectors.toList());
    }

    // --- Helper Validation & Mapping ---

    private void validateTaskOwnership(HousekeepingTask task, Long staffId) {
        if (staffId != null && task.getAssignedStaffId() != null && !task.getAssignedStaffId().equals(staffId)) {
            throw new ForbiddenException("Task " + task.getId() + " is assigned to another staff member");
        }
    }

    private void recordTaskHistory(Long taskId, TaskStatus oldStatus, TaskStatus newStatus, String changedBy, String remarks) {
        TaskHistory history = TaskHistory.builder()
                .taskId(taskId)
                .oldStatus(oldStatus != null ? oldStatus : TaskStatus.UNASSIGNED)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .remarks(remarks)
                .build();
        taskHistoryRepository.save(history);
    }

    private HousekeepingTaskResponse mapToTaskResponse(HousekeepingTask t) {
        return HousekeepingTaskResponse.builder()
                .id(t.getId())
                .taskCode(t.getTaskCode())
                .roomId(t.getRoomId())
                .reservationId(t.getReservationId())
                .serviceRequestId(t.getServiceRequestId())
                .taskType(t.getTaskType())
                .assignedStaffId(t.getAssignedStaffId())
                .priority(t.getPriority())
                .status(t.getStatus())
                .startedAt(t.getStartedAt())
                .completedAt(t.getCompletedAt())
                .verifiedAt(t.getVerifiedAt())
                .remarks(t.getRemarks())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private TaskHistoryResponse mapToTaskHistoryResponse(TaskHistory h) {
        return TaskHistoryResponse.builder()
                .id(h.getId())
                .taskId(h.getTaskId())
                .oldStatus(h.getOldStatus())
                .newStatus(h.getNewStatus())
                .changedBy(h.getChangedBy())
                .remarks(h.getRemarks())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private RoomAssignmentResponse mapToAssignmentResponse(RoomAssignment a) {
        return RoomAssignmentResponse.builder()
                .id(a.getId())
                .staffId(a.getStaffId())
                .startRoomNumber(a.getStartRoomNumber())
                .endRoomNumber(a.getEndRoomNumber())
                .effectiveFrom(a.getEffectiveFrom())
                .effectiveTo(a.getEffectiveTo())
                .active(a.getActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private MaintenanceResponse mapToMaintenanceResponse(MaintenanceIssue m) {
        return MaintenanceResponse.builder()
                .id(m.getId())
                .issueCode(m.getIssueCode())
                .roomId(m.getRoomId())
                .description(m.getDescription())
                .status(m.getStatus())
                .assignedStaffId(m.getAssignedStaffId())
                .cost(m.getCost())
                .reportedBy(m.getReportedBy())
                .resolvedAt(m.getResolvedAt())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private BreakageResponse mapToBreakageResponse(BreakageReport b) {
        return BreakageResponse.builder()
                .id(b.getId())
                .reportCode(b.getReportCode())
                .reservationId(b.getReservationId())
                .roomId(b.getRoomId())
                .staffId(b.getStaffId())
                .description(b.getDescription())
                .chargeAmount(b.getChargeAmount())
                .status(b.getStatus())
                .approvedByManagerId(b.getApprovedByManagerId())
                .remarks(b.getRemarks())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private ExpenseResponse mapToExpenseResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId())
                .category(e.getCategory())
                .amount(e.getAmount())
                .description(e.getDescription())
                .expenseDate(e.getExpenseDate())
                .recordedByStaffId(e.getRecordedByStaffId())
                .approvedByManagerId(e.getApprovedByManagerId())
                .status(e.getStatus())
                .remarks(e.getRemarks())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private UtilityResponse mapToUtilityResponse(UtilityRecord u) {
        return UtilityResponse.builder()
                .id(u.getId())
                .utilityType(u.getUtilityType())
                .periodMonth(u.getPeriodMonth())
                .periodYear(u.getPeriodYear())
                .unitsConsumed(u.getUnitsConsumed())
                .totalCost(u.getTotalCost())
                .remarks(u.getRemarks())
                .recordedAt(u.getRecordedAt())
                .build();
    }

    private CashDrawerResponse mapToCashDrawerResponse(CashDrawer c) {
        return CashDrawerResponse.builder()
                .id(c.getId())
                .drawerDate(c.getDrawerDate())
                .shiftId(c.getShiftId())
                .openedByStaffId(c.getOpenedByStaffId())
                .closedByStaffId(c.getClosedByStaffId())
                .openingBalance(c.getOpeningBalance())
                .cashReceived(c.getCashReceived())
                .cashExpenses(c.getCashExpenses())
                .expectedClosing(c.getExpectedClosing())
                .actualClosing(c.getActualClosing())
                .difference(c.getDifference())
                .status(c.getStatus())
                .remarks(c.getRemarks())
                .createdAt(c.getCreatedAt())
                .closedAt(c.getClosedAt())
                .build();
    }

    private ShiftHandoverResponse mapToShiftHandoverResponse(ShiftHandover s) {
        return ShiftHandoverResponse.builder()
                .id(s.getId())
                .outgoingStaffId(s.getOutgoingStaffId())
                .incomingStaffId(s.getIncomingStaffId())
                .shiftId(s.getShiftId())
                .pendingPayments(s.getPendingPayments())
                .arrivals(s.getArrivals())
                .departures(s.getDepartures())
                .pendingComplaints(s.getPendingComplaints())
                .pendingServiceRequests(s.getPendingServiceRequests())
                .pendingHousekeepingTasks(s.getPendingHousekeepingTasks())
                .pendingMaintenance(s.getPendingMaintenance())
                .cashInformation(s.getCashInformation())
                .remarks(s.getRemarks())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .acknowledgedAt(s.getAcknowledgedAt())
                .build();
    }

    private void updateRoomStatus(Long roomId, String status) {
        if (roomId == null) return;
        try {
            roomClient.updateRoomStatus(roomId, java.util.Map.of("status", status));
        } catch (Exception ex) {
            log.error("Could not update room status to {}: {}", status, ex.getMessage());
        }
    }

    private boolean isCleaningTask(TaskType type) {
        if (type == null) return false;
        return type == TaskType.CHECKOUT_CLEANING ||
               type == TaskType.DEEP_CLEANING ||
               type == TaskType.GUEST_REQUEST_CLEANING;
    }
}
