package com.hms.notificationservice.service;

import com.hms.notificationservice.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Page<NotificationResponse> getNotifications(String recipient, String role, Pageable pageable);
    NotificationResponse getNotificationById(Long id);
    NotificationResponse markAsRead(Long id);
}
