package com.hms.notificationservice.service;

import com.hms.notificationservice.dto.response.NotificationResponse;
import com.hms.notificationservice.entity.Notification;
import com.hms.notificationservice.exception.ResourceNotFoundException;
import com.hms.notificationservice.repository.NotificationRepository;
import com.hms.notificationservice.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
                .id(1L)
                .recipient("guest@hotel.com")
                .recipientRole("GUEST")
                .title("Reservation Confirmed")
                .message("Your reservation has been confirmed")
                .channel("EMAIL")
                .status("UNREAD")
                .referenceType("RESERVATION")
                .referenceId(100L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(testNotification), pageable, 1);

        when(notificationRepository.findByRecipientOrRecipientRoleOrderByCreatedAtDesc(eq("guest@hotel.com"), eq("GUEST"), eq(pageable)))
                .thenReturn(page);

        Page<NotificationResponse> result = notificationService.getNotifications("guest@hotel.com", "GUEST", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Reservation Confirmed", result.getContent().get(0).getTitle());
    }

    @Test
    void testGetNotificationById_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        NotificationResponse response = notificationService.getNotificationById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("UNREAD", response.getStatus());
    }

    @Test
    void testGetNotificationById_NotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.getNotificationById(999L));
    }

    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L);

        assertNotNull(response);
        assertEquals("READ", response.getStatus());
        verify(notificationRepository, times(1)).save(testNotification);
    }
}
