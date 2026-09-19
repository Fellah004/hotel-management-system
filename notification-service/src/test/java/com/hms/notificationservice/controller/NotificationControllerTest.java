package com.hms.notificationservice.controller;

import com.hms.notificationservice.dto.response.NotificationResponse;
import com.hms.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        testResponse = NotificationResponse.builder()
                .id(1L)
                .recipient("guest@hotel.com")
                .recipientRole("GUEST")
                .title("Reservation Confirmed")
                .message("Your reservation has been confirmed")
                .channel("EMAIL")
                .status("UNREAD")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetNotifications_Success() throws Exception {
        when(notificationService.getNotifications(nullable(String.class), nullable(String.class), any()))
                .thenReturn(new PageImpl<>(List.of(testResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Reservation Confirmed"));
    }

    @Test
    void testGetNotificationById_Success() throws Exception {
        when(notificationService.getNotificationById(1L)).thenReturn(testResponse);

        mockMvc.perform(get("/api/notifications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("UNREAD"));
    }

    @Test
    void testMarkAsRead_Success() throws Exception {
        testResponse.setStatus("READ");
        when(notificationService.markAsRead(1L)).thenReturn(testResponse);

        mockMvc.perform(put("/api/notifications/1/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("READ"));
    }
}
