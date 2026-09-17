package com.hms.operationsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hms.operationsservice.dto.request.CreateHousekeepingTaskRequest;
import com.hms.operationsservice.dto.response.HousekeepingTaskResponse;
import com.hms.operationsservice.entity.TaskStatus;
import com.hms.operationsservice.entity.TaskType;
import com.hms.operationsservice.service.OperationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HousekeepingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OperationsService operationsService;

    @InjectMocks
    private HousekeepingController housekeepingController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(housekeepingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateTask() throws Exception {
        CreateHousekeepingTaskRequest request = CreateHousekeepingTaskRequest.builder()
                .roomId(101L)
                .taskType(TaskType.CHECKOUT_CLEANING)
                .priority("HIGH")
                .build();

        HousekeepingTaskResponse response = HousekeepingTaskResponse.builder()
                .id(1L)
                .taskCode("TASK-101")
                .roomId(101L)
                .taskType(TaskType.CHECKOUT_CLEANING)
                .priority("HIGH")
                .status(TaskStatus.ASSIGNED)
                .createdAt(LocalDateTime.now())
                .build();

        when(operationsService.createTask(any(CreateHousekeepingTaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/housekeeping/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.taskCode").value("TASK-101"))
                .andExpect(jsonPath("$.taskType").value("CHECKOUT_CLEANING"));
    }

    @Test
    void testCompleteTask() throws Exception {
        HousekeepingTaskResponse response = HousekeepingTaskResponse.builder()
                .id(1L)
                .taskCode("TASK-101")
                .roomId(101L)
                .taskType(TaskType.CHECKOUT_CLEANING)
                .status(TaskStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        when(operationsService.completeTask(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/housekeeping/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
