package com.hms.guestexperienceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hms.guestexperienceservice.dto.request.CreateServiceRequest;
import com.hms.guestexperienceservice.dto.response.ServiceRequestResponse;
import com.hms.guestexperienceservice.entity.RequestStatus;
import com.hms.guestexperienceservice.entity.RequestType;
import com.hms.guestexperienceservice.service.GuestExperienceService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GuestExperienceService guestExperienceService;

    @InjectMocks
    private ServiceRequestController serviceRequestController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(serviceRequestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateServiceRequest() throws Exception {
        CreateServiceRequest request = CreateServiceRequest.builder()
                .reservationId(101L)
                .guestId(202L)
                .roomId(303L)
                .requestType(RequestType.ROOM_CLEANING)
                .description("Morning cleanup")
                .quantity(1)
                .priority("NORMAL")
                .build();

        ServiceRequestResponse response = ServiceRequestResponse.builder()
                .id(1L)
                .requestCode("REQ-101")
                .reservationId(101L)
                .guestId(202L)
                .roomId(303L)
                .requestType(RequestType.ROOM_CLEANING)
                .description("Morning cleanup")
                .quantity(1)
                .priority("NORMAL")
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(guestExperienceService.createServiceRequest(any(CreateServiceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/service-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.requestCode").value("REQ-101"))
                .andExpect(jsonPath("$.requestType").value("ROOM_CLEANING"));
    }

    @Test
    void testGetServiceRequestById() throws Exception {
        ServiceRequestResponse response = ServiceRequestResponse.builder()
                .id(1L)
                .requestCode("REQ-101")
                .reservationId(101L)
                .guestId(202L)
                .roomId(303L)
                .requestType(RequestType.FOOD_ORDER)
                .description("Continental Breakfast")
                .status(RequestStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();

        when(guestExperienceService.getServiceRequestById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/service-requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.requestType").value("FOOD_ORDER"));
    }
}
