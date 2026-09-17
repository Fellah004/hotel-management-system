package com.hms.operationsservice.service;

import com.hms.operationsservice.client.RoomClient;
import com.hms.operationsservice.dto.request.*;
import com.hms.operationsservice.dto.response.*;
import com.hms.operationsservice.entity.*;
import com.hms.operationsservice.event.publisher.OperationsEventPublisher;
import com.hms.operationsservice.repository.*;
import com.hms.operationsservice.service.impl.OperationsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationsServiceTest {

    @Mock
    private HousekeepingTaskRepository taskRepository;

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @Mock
    private RoomAssignmentRepository roomAssignmentRepository;

    @Mock
    private MaintenanceIssueRepository maintenanceRepository;

    @Mock
    private BreakageReportRepository breakageRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UtilityRecordRepository utilityRepository;

    @Mock
    private CashDrawerRepository cashDrawerRepository;

    @Mock
    private ShiftHandoverRepository shiftHandoverRepository;

    @Mock
    private OperationsEventPublisher eventPublisher;

    @Mock
    private RoomClient roomClient;

    @InjectMocks
    private OperationsServiceImpl operationsService;

    private HousekeepingTask sampleTask;
    private BreakageReport sampleBreakage;
    private CashDrawer sampleDrawer;

    @BeforeEach
    void setUp() {
        sampleTask = HousekeepingTask.builder()
                .id(1L)
                .taskCode("TASK-12345678")
                .roomId(101L)
                .taskType(TaskType.CHECKOUT_CLEANING)
                .assignedStaffId(10L)
                .priority("HIGH")
                .status(TaskStatus.ASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleBreakage = BreakageReport.builder()
                .id(1L)
                .reportCode("BRK-12345678")
                .reservationId(100L)
                .roomId(101L)
                .staffId(10L)
                .description("Broken television remote")
                .chargeAmount(new BigDecimal("35.00"))
                .status("REPORTED")
                .createdAt(LocalDateTime.now())
                .build();

        sampleDrawer = CashDrawer.builder()
                .id(1L)
                .drawerDate(LocalDate.now())
                .openingBalance(new BigDecimal("500.00"))
                .cashReceived(BigDecimal.ZERO)
                .cashExpenses(BigDecimal.ZERO)
                .expectedClosing(new BigDecimal("500.00"))
                .status(CashDrawerStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create task with auto-assignment from room assignments")
    void testCreateTask_WithAutoAssignment() {
        CreateHousekeepingTaskRequest request = CreateHousekeepingTaskRequest.builder()
                .roomId(25L)
                .taskType(TaskType.CHECKOUT_CLEANING)
                .priority("HIGH")
                .build();

        RoomAssignment assignment = RoomAssignment.builder()
                .id(1L)
                .staffId(10L)
                .startRoomNumber(1)
                .endRoomNumber(50)
                .effectiveFrom(LocalDate.now())
                .active(true)
                .build();

        when(roomAssignmentRepository.findActiveAssignmentsForRoom(eq(25), any())).thenReturn(List.of(assignment));
        when(taskRepository.save(any(HousekeepingTask.class))).thenReturn(sampleTask);

        HousekeepingTaskResponse response = operationsService.createTask(request);

        assertNotNull(response);
        assertEquals(TaskType.CHECKOUT_CLEANING, response.getTaskType());
        verify(taskRepository, times(1)).save(any(HousekeepingTask.class));
        verify(eventPublisher, times(1)).publishTaskAssigned(any(HousekeepingTask.class));
    }

    @Test
    @DisplayName("Should progress task: Accept -> Start (Room CLEANING) -> Complete (Room CLEAN) -> Verify (Room AVAILABLE)")
    void testHousekeepingTaskFullLifecycle() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(HousekeepingTask.class))).thenReturn(sampleTask);

        // 1. Accept
        HousekeepingTaskResponse acceptResp = operationsService.acceptTask(1L, 10L);
        assertNotNull(acceptResp);
        assertEquals(TaskStatus.ACCEPTED, sampleTask.getStatus());

        // 2. Start
        HousekeepingTaskResponse startResp = operationsService.startTask(1L, 10L);
        assertNotNull(startResp);
        assertEquals(TaskStatus.IN_PROGRESS, sampleTask.getStatus());
        verify(roomClient, times(1)).updateRoomStatus(101L, Map.of("status", "CLEANING"));

        // 3. Complete
        HousekeepingTaskResponse completeResp = operationsService.completeTask(1L, 10L);
        assertNotNull(completeResp);
        assertEquals(TaskStatus.COMPLETED, sampleTask.getStatus());
        verify(roomClient, times(1)).updateRoomStatus(101L, Map.of("status", "CLEAN"));
        verify(eventPublisher, times(1)).publishTaskCompleted(any(HousekeepingTask.class));

        // 4. Verify by Manager
        HousekeepingTaskResponse verifyResp = operationsService.verifyTask(1L, 2L);
        assertNotNull(verifyResp);
        assertEquals(TaskStatus.VERIFIED, sampleTask.getStatus());
        verify(roomClient, times(1)).updateRoomStatus(101L, Map.of("status", "AVAILABLE"));
    }

    @Test
    @DisplayName("Should approve breakage and publish BreakageApproved event")
    void testReviewBreakage_Approve() {
        ReviewBreakageRequest reviewReq = ReviewBreakageRequest.builder()
                .status("APPROVED")
                .managerId(2L)
                .remarks("Approved for guest charge")
                .build();

        when(breakageRepository.findById(1L)).thenReturn(Optional.of(sampleBreakage));
        when(breakageRepository.save(any(BreakageReport.class))).thenReturn(sampleBreakage);

        BreakageResponse response = operationsService.reviewBreakage(1L, reviewReq);

        assertNotNull(response);
        assertEquals("APPROVED", sampleBreakage.getStatus());
        verify(eventPublisher, times(1)).publishBreakageApproved(any(BreakageReport.class));
    }

    @Test
    @DisplayName("Should reconcile cash drawer on close")
    void testCloseCashDrawer_Reconcile() {
        CloseCashDrawerRequest closeReq = CloseCashDrawerRequest.builder()
                .staffId(10L)
                .cashReceived(new BigDecimal("300.00"))
                .cashExpenses(new BigDecimal("50.00"))
                .actualClosing(new BigDecimal("750.00")) // Expected: 500 + 300 - 50 = 750
                .remarks("Perfect match")
                .build();

        when(cashDrawerRepository.findById(1L)).thenReturn(Optional.of(sampleDrawer));
        when(cashDrawerRepository.save(any(CashDrawer.class))).thenReturn(sampleDrawer);

        CashDrawerResponse response = operationsService.closeCashDrawer(1L, closeReq);

        assertNotNull(response);
        assertEquals(CashDrawerStatus.CLOSED, sampleDrawer.getStatus());
        assertEquals(new BigDecimal("750.00"), sampleDrawer.getExpectedClosing());
        assertEquals(0, sampleDrawer.getDifference().compareTo(BigDecimal.ZERO));
    }
}
